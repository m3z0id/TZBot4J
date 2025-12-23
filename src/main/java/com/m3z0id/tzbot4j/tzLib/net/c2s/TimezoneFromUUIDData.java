package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TimezoneFromUUIDData implements Identifiable {
    private UUID uuid;
    public TimezoneFromUUIDData(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    @Override
    public String getRequestType() {
        return "TIMEZONE_FROM_UUID";
    }
}
