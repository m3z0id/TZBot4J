package com.m3z0id.tzbot4j.config.subclasses;

import com.m3z0id.tzbot4j.config.uint16_t;

public class APIConfig {
    private String address;
    private uint16_t port;
    private String apiKey;
    private String encryptionKey;
    private boolean gunzip;
    int retryAfterIfInaccessible;

    public boolean validate() {
        return address != null && port != null && apiKey != null && retryAfterIfInaccessible > 0;
    }
    public String getAddress() { return address; }
    public uint16_t getPort() { return port; }
    public String getApiKey() { return apiKey; }
    public int getRetryAfterIfInaccessible() { return retryAfterIfInaccessible; }
    public String getEncryptionKey() { return encryptionKey; }
    public boolean shouldGunzip() { return gunzip; }
}
