package com.m3z0id.tzbot4j;

import com.m3z0id.tzbot4j.config.Config;
import com.m3z0id.tzbot4j.tzLib.net.TZBotClient;
import com.m3z0id.tzbot4j.tzLib.net.TZRequest;
import com.m3z0id.tzbot4j.tzLib.net.TZResponse;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromIPData;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezonePostData;
import com.m3z0id.tzbot4j.tzLib.TimezoneManager;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromUUIDData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TZBot4J {
    private static TZBot4J INSTANCE;

    private final Logger LOGGER;
    private final Path dataDir;
    private final Config config;

    private final ScheduledExecutorService scheduler;

    private final TZBotClient client;
    private final TimezoneManager manager;

    private final HttpClient webhookClient = HttpClient.newHttpClient();
    private final AtomicBoolean isServiceUp = new AtomicBoolean(false);
    private final List<UUID> timezoneQueue = new ArrayList<>();

    public static TZBot4J init(Logger logger, Path dataDir) {
        if(INSTANCE != null) return INSTANCE;
        return new TZBot4J(logger, dataDir);
    }

    public void close() {
        LOGGER.info("TZBot4J is shutting down!");
        scheduler.close();
        webhookClient.close();
        try {
            client.close();
        } catch (IOException ignored) {}
    }

    public void addPlayer(UUID uuid, String fallbackIP) {
        if(!isServiceUp.get() || timezoneQueue.contains(uuid)) return;
        TZResponse resp = client.send(new TZRequest(new TimezoneFromUUIDData(uuid)));
        if(resp == null || !resp.isSuccessful()) {
            TZResponse response = client.send(new TZRequest(new TimezoneFromIPData(fallbackIP)));
            if(response == null || !response.isSuccessful()) timezoneQueue.add(uuid);
            else getTZManager().addTimezone(uuid, response.getAsString());
        } else getTZManager().addTimezone(uuid, resp.getAsString());
    }

    public TimezoneManager getTZManager() {
        return manager;
    }

    public @Nullable TZResponse send(TZRequest request) {
        return client.send(request);
    }

    public boolean isTZBotUp() {
        return isServiceUp.get();
    }

    private TZBot4J(Logger logger, Path dataDir) {
        LOGGER = logger;
        this.dataDir = dataDir;

        if(!this.dataDir.toFile().exists() || !this.dataDir.toFile().isDirectory()) {
            this.dataDir.toFile().mkdirs();
        }

        config = Config.get(this.dataDir.toFile());
        if(config == null) throw new RuntimeException("Config couldn't be initialized.");

        try {
            client = new TZBotClient(config.getTzBot().getAddress(), config.getTzBot().getPort(), config.getTzBot().getEncryptionKey(), config.getTzBot().getApiKey());
        } catch (Exception e) {
            throw new RuntimeException("TZBot client couldn't be initialized. Error: ", e);
        }

        manager = new TimezoneManager();

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            isServiceUp.set(client.isTzBotUp());
            if(!isServiceUp.get()) sendServiceUnavailableMsg();
        }, 0, config.getTzBot().getRetryAfterIfInaccessible(), TimeUnit.MINUTES);

        LOGGER.info("TZBot4J fully initialized!");
    }

    private void sendServiceUnavailableMsg() {
        LOGGER.error("TZBot is unavailable!");
        if(!config.getWebhook().getSendUnavailableMessages()) return;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(config.getWebhook().getUrl())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"content\": \"%s\"}".formatted(config.getWebhook().getServiceUnavailableMessage())))
                .build();

        try {
            HttpResponse<String> response = webhookClient.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) LOGGER.error("Discord API is unavailable!");
            else {
                for(UUID uuid : List.copyOf(timezoneQueue)) {
                    TZResponse resp = client.send(new TZRequest(new TimezoneFromUUIDData(uuid)));
                    if(resp != null && resp.isSuccessful()) manager.addTimezone(uuid, resp.getAsString());
                    else LOGGER.error("Got a bad response! UUID: %s".formatted(uuid));
                    timezoneQueue.remove(uuid);
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Discord API is unavailable! Error: %s".formatted(e.getMessage()));
        }
    }
}
