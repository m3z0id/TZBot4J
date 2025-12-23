package com.m3z0id.tzbot4j.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class uint16_t implements JsonDeserializer<uint16_t> {
    private short val;
    public uint16_t(int val) throws  IllegalArgumentException {
        if(!predicate(val)) throw new IllegalArgumentException("Invalid value: %d".formatted(val));
        this.val = (short)(val - 32768);
    }

    public int get() {
        return (val + 32768);
    }

    public void set(int val) throws IllegalArgumentException {
        if(!predicate(val)) throw new IllegalArgumentException("Invalid value: %d".formatted(val));
        this.val = (short)(val - 32768);
    }

    public static boolean predicate(int newVal) {
        return !(newVal > (Short.MAX_VALUE + 32768) || newVal < 0);
    }

    @Override
    public uint16_t deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new uint16_t(json.getAsInt());
    }
}
