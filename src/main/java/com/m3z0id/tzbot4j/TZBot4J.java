package com.m3z0id.tzbot4j;

import com.m3z0id.tzbot4j.config.Config;
import com.m3z0id.tzbot4j.config.subclasses.TZFlag;
import com.m3z0id.tzbot4j.tzLib.net.TZBotClient;
import com.m3z0id.tzbot4j.tzLib.net.TZRequest;
import com.m3z0id.tzbot4j.tzLib.net.TZResponse;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromIPData;
import com.m3z0id.tzbot4j.tzLib.TimezoneManager;
import com.m3z0id.tzbot4j.tzLib.net.c2s.TimezoneFromUUIDData;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class TZBot4J {
    private static TZBot4J INSTANCE;

    public final Logger LOGGER;
    public final Config config;

    private final TZBotClient client;
    private final TimezoneManager manager;

    public static TZBot4J init(Logger logger, Config config, TZFlag... tzFlags) {
        if(INSTANCE != null) return INSTANCE;
        return new TZBot4J(logger, config, tzFlags);
    }

    public void close() {
        LOGGER.info("TZBot4J is shutting down!");
        try {
            client.close();
        } catch (IOException ignored) {}
    }

    public void addPlayer(UUID uuid, String fallbackIP, Predicate<UUID> isStillOnline) {
        CompletableFuture<TZResponse> uuidFuture = client.queueRequestInternal(new TZRequest(new TimezoneFromUUIDData(uuid)));
        CompletableFuture<TZResponse> ipFuture = client.queueRequestInternal(new TZRequest(new TimezoneFromIPData(fallbackIP)));

        uuidFuture.whenComplete((response, exception) -> {
            if(exception == null && response.isSuccessful() && isStillOnline.test(uuid)) manager.addTimezone(uuid, response.getAsString());
        });
        ipFuture.whenComplete((response, exception) -> {
            if(exception == null && response.isSuccessful() && isStillOnline.test(uuid)) manager.addTimezone(uuid, response.getAsString());
        });
    }

    public void removePlayer(UUID uuid) {
        manager.removeTimezone(uuid);
    }
    public TimezoneManager getTZManager() {
        return manager;
    }
    private TZBot4J(Logger logger, Config tzConfig, TZFlag... flags) {
        LOGGER = logger;
        this.config = tzConfig;

        try {
            client = new TZBotClient(config.getTzBot().getAddress(), config.getTzBot().getPort(), config.getTzBot().getEncryptionKey(), config.getTzBot().getApiKey(), this, flags);
        } catch (Exception e) {
            throw new RuntimeException("TZBot client couldn't be initialized. Error: ", e);
        }

        manager = new TimezoneManager();
        LOGGER.info("TZBot4J fully initialized!");
    }

    public CompletableFuture<TZResponse> queueRequest(TZRequest request) {
        return client.queueRequestInternal(request);
    }

    public boolean isTZBotUp() {
        return client.isTzBotUp();
    }
}
