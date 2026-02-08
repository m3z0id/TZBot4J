package com.m3z0id.tzbot4j.tzLib.net.c2s;

public class UUIDFromUserID implements Identifiable {
    private long userId;

    public UUIDFromUserID(long userId) {
        this.userId = userId;
    }

    @Override
    public byte getRequestId() {
        return 7;
    }
}
