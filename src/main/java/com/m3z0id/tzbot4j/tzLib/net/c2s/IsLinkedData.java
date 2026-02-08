package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class IsLinkedData implements Identifiable {
    private UUID uuid;
    public IsLinkedData(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public byte getRequestId() {
        return 5;
    }
}
