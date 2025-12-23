package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

public class PingData implements Identifiable {
    @NotNull
    @Override
    public String getRequestType() {
        return "PING";
    }
}
