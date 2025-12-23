package com.m3z0id.tzbot4j.tzLib.net;

import com.google.gson.Gson;
import com.m3z0id.tzbot4j.config.uint16_t;
import com.m3z0id.tzbot4j.network.AES256Factory;
import com.m3z0id.tzbot4j.network.TCPSocket;
import com.m3z0id.tzbot4j.network.UDPSocket;
import com.m3z0id.tzbot4j.tzLib.net.c2s.PingData;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TZBotClient {
    private final TCPSocket tcp;
    private final UDPSocket udp;
    private final String apiKey;
    private AES256Factory factory;
    private final Gson gson;

    public TZBotClient(String hostname, uint16_t port, String encryptionKey, String apiKey) throws SocketException, UnknownHostException {
        tcp = new TCPSocket(hostname, port.get());
        udp = new UDPSocket(hostname, port.get());

        tcp.setTimeout(5000);
        udp.setTimeout(5000);

        if(encryptionKey != null && !encryptionKey.isEmpty()) factory = new AES256Factory(encryptionKey);
        this.apiKey = apiKey;
        gson = new Gson();
    }

    public @Nullable TZResponse send(TZRequest request) {
        request.setApiKey(apiKey);
        String requestStr = gson.toJson(request);
        byte[] requestBytes;
        if(factory != null) {
            try {
                requestBytes = factory.encrypt(requestStr);
            } catch (Exception e) {
                requestBytes = requestStr.getBytes();
            }
        } else {
            requestBytes = requestStr.getBytes();
        }

        byte[] response = udp.makeRequest(requestBytes);
        if(response == null) response = tcp.makeRequest(requestBytes);

        if(factory != null) {
            try {
                return gson.fromJson(factory.decrypt(response), TZResponse.class);
            } catch (Exception e) {
                return null;
            }
        }

        return gson.fromJson(new String(response), TZResponse.class);
    }

    public boolean isTzBotUp() {
        TZResponse pingResponse = this.send(new TZRequest(new PingData()));
        return pingResponse != null && pingResponse.isSuccessful();
    }

    public void close() throws IOException {
        tcp.close();
        udp.close();
    }
}
