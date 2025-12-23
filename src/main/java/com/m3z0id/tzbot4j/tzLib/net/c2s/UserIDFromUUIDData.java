package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserIDFromUUIDData implements Identifiable {
    private UUID uuid;
    public UserIDFromUUIDData(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    @Override
    public String getRequestType() {
        return "USER_ID_FROM_UUID";
    }
}
