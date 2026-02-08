package com.m3z0id.tzbot4j.tzLib.net;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.m3z0id.tzbot4j.TZBot4J;
import com.m3z0id.tzbot4j.config.subclasses.Pair;
import com.m3z0id.tzbot4j.config.subclasses.TZFlag;
import com.m3z0id.tzbot4j.config.uint16_t;
import com.m3z0id.tzbot4j.exception.RequestFailedException;
import com.m3z0id.tzbot4j.exception.RequestNotNeededException;
import com.m3z0id.tzbot4j.factory.AES256Factory;
import com.m3z0id.tzbot4j.factory.ChaCha20Factory;
import com.m3z0id.tzbot4j.factory.GunzipFactory;
import com.m3z0id.tzbot4j.network.TCPSocket;
import com.m3z0id.tzbot4j.network.UDPSocket;
import com.m3z0id.tzbot4j.tzLib.net.c2s.PingData;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromIPData;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromUUIDData;
import org.jetbrains.annotations.Nullable;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TZBotClient {
    private final TCPSocket tcp;
    private final UDPSocket udp;
    private final String apiKey;
    private AES256Factory aes;
    private ChaCha20Factory chacha;
    private final GunzipFactory gunzip = new GunzipFactory();
    private final ObjectMapper jsonMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    private final ObjectMapper packMapper = new ObjectMapper(new MessagePackFactory())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY))
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    private final TZBot4J instance;
    private final Thread eventLoopThread;
    private final HttpClient webhookClient = HttpClient.newHttpClient();

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running;
    private final AtomicInteger failedAmount;
    private final AtomicBoolean isServiceUp;
    private final LinkedBlockingDeque<Pair<TZRequest, CompletableFuture<TZResponse>>> requestQueue;
    private int targetFlags;

    public TZBotClient(String hostname, uint16_t port, String encryptionKey, String apiKey, TZBot4J instance, TZFlag... targetFlags) throws SocketException, UnknownHostException {
        tcp = new TCPSocket(hostname, port.get());
        udp = new UDPSocket(hostname, port.get());

        tcp.setTimeout(2000);
        udp.setTimeout(2000);

        if(encryptionKey != null && !encryptionKey.isEmpty()) {
            aes = new AES256Factory(encryptionKey);
            chacha = new ChaCha20Factory(encryptionKey);
        }

        this.apiKey = apiKey;
        this.instance = instance;

        requestQueue = new LinkedBlockingDeque<>();
        running = new AtomicBoolean(true);
        isServiceUp = new AtomicBoolean(false);

        failedAmount = new AtomicInteger(0);

        AtomicLong pingCounter = new AtomicLong();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            pingCounter.getAndIncrement();
            if (this.requestQueue.isEmpty()) queueRequestInternal(new TZRequest(new PingData()));
        }, 0, 60, TimeUnit.SECONDS);

        for (TZFlag flag : targetFlags) {
            this.targetFlags |= flag.getVal();
        }

        eventLoopThread = new Thread(this::eventLoop, "TZEventThread");
        eventLoopThread.start();
    }

    private void handleSuccess() {
        this.isServiceUp.set(true);
        this.failedAmount.set(0);
    }

    private void handleFail(SocketException e) {
        this.isServiceUp.set(false);
        instance.LOGGER.warn("TZBot error: {}", e.getMessage());

        int failed = failedAmount.incrementAndGet();
        if ((failed * 6) % instance.config.getTzBot().getRetryAfterIfInaccessible() == 0) sendServiceUnavailableMsg();
    }

    private byte[] applyFlags(TZRequest request) {
        if(!(request.getData() instanceof PingData)) request.setApiKey(apiKey);
        byte[] byteRepr;
        try {
            byteRepr = jsonMapper.writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            return null;
        }


        byte[] header = new byte[7];
        header[0] = 't';
        header[1] = 'z';
        header[2] = 7;
        header[3] = request.getData().getRequestId();
        header[4] = (byte) targetFlags;

        if((targetFlags & TZFlag.MSGPACK.getVal()) != 0) {
            try {
                byteRepr = packMapper.writeValueAsBytes(request);
            } catch (JsonProcessingException ignored) {}
        }
        if((targetFlags & TZFlag.GZIP.getVal()) != 0) {
            try {
                byteRepr = gunzip.compress(byteRepr);
            } catch (IOException ignored) {}
        }
        if((targetFlags & TZFlag.AES.getVal()) != 0 || (targetFlags & TZFlag.CHACHA20.getVal()) != 0) {
            header[5] = (byte) (((byteRepr.length + 28) >> 8) & 0xFF);
            header[6] = (byte) ((byteRepr.length + 28) & 0xFF);
            if((targetFlags & TZFlag.AES.getVal()) != 0) {
                try {
                    byteRepr = aes.encrypt(byteRepr, header);
                } catch (Exception ignored) {}
            } else if ((targetFlags & TZFlag.CHACHA20.getVal()) != 0) {
                try {
                    byteRepr = chacha.encrypt(byteRepr, header);
                } catch (Exception ignored) {}
            }
        } else {
            header[5] = (byte) ((byteRepr.length >> 8) & 0xFF);
            header[6] = (byte) (byteRepr.length & 0xFF);
        }

        byte[] returnVal = new byte[header.length + byteRepr.length];
        System.arraycopy(header, 0, returnVal, 0, header.length);
        System.arraycopy(byteRepr, 0, returnVal, header.length, byteRepr.length);
        return returnVal;
    }

    @Nullable
    private TZResponse rebuildFromHeaders(byte[] resp) {
        if (resp.length < 6) return null;
        if (resp[0] != 't' || resp[1] != 'z' || resp[2] != 6) return null;

        byte[] header = new byte[6];
        System.arraycopy(resp, 0, header, 0, 6);
        byte flags = header[3];

        int packetLen = ((header[4] & 0xFF) << 8) | (header[5] & 0xFF);

        if(resp.length != packetLen + 6) return null;
        byte[] body = new byte[packetLen];
        System.arraycopy(resp, 6, body, 0, packetLen);

        if((flags & TZFlag.AES.getVal()) != 0) {
            try {
                body = aes.decrypt(body, header);
            } catch (Exception ignored) {
                System.out.println("Failed to decrypt!");
                return null;
            }
        } else if((flags & TZFlag.CHACHA20.getVal()) != 0) {
            try {
                body = chacha.decrypt(body, header);
            } catch (Exception ignored) {
                return null;
            }
        }

        if((flags & TZFlag.GZIP.getVal()) != 0) {
            try {
                body = gunzip.decompress(body);
            } catch (Exception ignored) {
                return null;
            }
        }

        if((flags & TZFlag.MSGPACK.getVal()) != 0) {
            try {
                return packMapper.readValue(body, TZResponse.class);
            } catch (IOException ignored) {
                return null;
            }
        } else {
            try {
                return jsonMapper.readValue(body, TZResponse.class);
            } catch (IOException ignored) {
                return null;
            }
        }
    }

    private TZResponse send(TZRequest request) throws SocketException {
        byte[] finalRequest = applyFlags(request);
        if(finalRequest == null) return null;

        byte[] response = udp.makeRequest(finalRequest);
        if(response == null) throw new SocketException("Unable to get response from TZBot.");

        return rebuildFromHeaders(response);
    }

    public boolean isTzBotUp() {
        return isServiceUp.get();
    }

    public void close() throws IOException {
        tcp.close();
        udp.close();
        scheduler.close();
        running.set(false);
        eventLoopThread.interrupt();
    }

    public CompletableFuture<TZResponse> queueRequestInternal(TZRequest request) {
        CompletableFuture<TZResponse> future = new CompletableFuture<>();
        requestQueue.add(new Pair<>(request, future));
        return future;
    }

    private void eventLoop() {
        try {
            while (running.get()) {
                Pair<TZRequest, CompletableFuture<TZResponse>> poll = null;
                try {
                    poll = requestQueue.take();
                    TZRequest request = poll.getFirst();
                    TZResponse response = send(request);
                    if (response == null) {
                        poll.getSecond().completeExceptionally(new RequestFailedException(""));
                        continue;
                    }
                    poll.getSecond().complete(response);
                    this.handleSuccess();

                    if (request.getData() instanceof TimezoneFromUUIDData) {
                        if (response.isSuccessful()) {
                            Pair<TZRequest, CompletableFuture<TZResponse>> next = requestQueue.peek();
                            if (next != null && next.getFirst().getData() instanceof TimezoneFromIPData) {
                                next.getSecond().completeExceptionally(new RequestNotNeededException("Already resolved by UUID"));
                                requestQueue.take();
                            }
                        }
                    }
                } catch (SocketException e) {
                    if (!poll.getSecond().isDone()) requestQueue.addFirst(poll);
                    this.handleFail(e);
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendServiceUnavailableMsg() {
        instance.LOGGER.error("TZBot is unavailable!");
        if(!instance.config.getWebhook().getSendUnavailableMessages()) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(instance.config.getWebhook().getUrl())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"content\": \"%s\"}".formatted(instance.config.getWebhook().getServiceUnavailableMessage())))
                .build();

        try {
            HttpResponse<String> response = webhookClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(!String.valueOf(response.statusCode()).startsWith("2")) instance.LOGGER.error("Discord API is unavailable!");
        } catch (InterruptedException | IOException e) {
            instance.LOGGER.error("Discord API is unavailable! Error: %s".formatted(e.getMessage()));
        }
    }
}
