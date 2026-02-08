package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

public class TimezoneFromIPData implements Identifiable {
    private String ip;
    public TimezoneFromIPData(@NotNull String ip) {
        this.ip = ip;
    }

    @Override
    public byte getRequestId() {
        return 2;
    }
}
