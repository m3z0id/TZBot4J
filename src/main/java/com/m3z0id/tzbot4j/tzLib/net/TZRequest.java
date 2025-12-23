package com.m3z0id.tzbot4j.tzLib.net;


import com.m3z0id.tzbot4j.tzLib.net.c2s.Identifiable;

public class TZRequest {
    private String requestType;
    private String apiKey;
    private Identifiable data;

    public TZRequest(Identifiable data) {
        this.requestType = data.getRequestType();
        this.apiKey = "";
        this.data = data;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
