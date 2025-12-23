package com.m3z0id.tzbot4j.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m3z0id.tzbot4j.TZBot4J;
import com.m3z0id.tzbot4j.config.subclasses.APIConfig;
import com.m3z0id.tzbot4j.config.subclasses.WebhookConfig;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Objects;

public class Config {
    private APIConfig tzBot;
    private WebhookConfig webhook;

    public boolean validate() {
        return tzBot != null && webhook != null && tzBot.validate() && webhook.validate();
    }
    public APIConfig getTzBot() { return tzBot; }
    public WebhookConfig getWebhook() { return webhook; }

    public static Config get(File dataFolder) {
        File conf = new File(dataFolder, "tzConfig.json");
        if(!conf.exists()) {
            try {
                Files.copy(Objects.requireNonNull(TZBot4J.class.getResourceAsStream("/tzConfig.json")), conf.toPath());
            } catch (IOException e) {
                return null;
            }
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(uint16_t.class, new uint16_t(0))
                .create();

        try(Reader reader = new FileReader(conf)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            return null;
        }
    }
}
