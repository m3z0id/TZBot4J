package com.m3z0id.tzbot4j.util;

import com.google.gson.*;
import com.m3z0id.tzbot4j.tzLib.net.c2s.Identifiable;

import java.lang.reflect.Type;

public class BodyEmptySkippingSerializer implements JsonSerializer<Identifiable> {
    private final Gson delegate = new Gson();

    @Override
    public JsonElement serialize(Identifiable src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement element = delegate.toJsonTree(src);

        if (element.isJsonObject() && element.getAsJsonObject().isEmpty()) return JsonNull.INSTANCE;
        if (element.isJsonArray() && element.getAsJsonArray().isEmpty()) return JsonNull.INSTANCE;

        return element;
    }
}
