package com.m3z0id.tzbot4j.config.subclasses;

import java.net.URI;

public class WebhookConfig {
    URI url;
    boolean sendUnavailableMessages;
    String serviceUnavailableMessage;

    public boolean validate() {
        return url != null && serviceUnavailableMessage != null;
    }

    public URI getUrl() { return url; }
    public boolean getSendUnavailableMessages() { return sendUnavailableMessages; }
    public String getServiceUnavailableMessage() { return serviceUnavailableMessage; }
}
