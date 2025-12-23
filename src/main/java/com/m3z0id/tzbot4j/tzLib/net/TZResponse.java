package com.m3z0id.tzbot4j.tzLib.net;

import org.jetbrains.annotations.Nullable;

public class TZResponse {
    int code;
    Object message;

    public boolean isSuccessful() {
        return code >= 200 && code <= 300;
    }
    public @Nullable String getAsString() {
        if(message instanceof String msgString) return msgString;
        else return null;
    }
    public int getAsInt() {
        if(message instanceof Integer msgInt) return msgInt;
        else return 0;
    }
}
