package com.m3z0id.tzbot4j.tzLib.net;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.m3z0id.tzbot4j.tzLib.net.c2s.Identifiable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TZRequest {
    private String apiKey;
    private Identifiable data;

    public TZRequest(Identifiable data) {
        this.apiKey = "";
        this.data = data;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public Identifiable getData() {
        return data;
    }
}
