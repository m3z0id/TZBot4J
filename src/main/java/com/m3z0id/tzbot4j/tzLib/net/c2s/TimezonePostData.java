package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TimezonePostData implements Identifiable {
    private UUID uuid;
    private String timezone;

    public TimezonePostData(@NotNull UUID uuid, @NotNull String timezone) {
        this.uuid = uuid;
        this.timezone = timezone;
    }

    @NotNull
    @Override
    public String getRequestType() {
        return "USER_ID_UUID_LINK_POST";
    }
}
