package com.m3z0id.tzbot4j.config.subclasses;

public enum TZFlag {
    AES(1),
    CHACHA20(1 << 1),
    GZIP(1 << 2),
    MSGPACK(1 << 3);

    private int val;
    TZFlag(int repr) {
        this.val = repr;
    }

    public int getVal() {
        return val;
    }
}
