package com.m3z0id.tzbot4j.util;

import com.google.gson.*;
import com.m3z0id.tzbot4j.tzLib.net.TZRequest;

import java.lang.reflect.Type;

public class RequestEmptySkippingSerializer implements JsonSerializer<TZRequest> {
    private final Gson delegate = new Gson();

    @Override
    public JsonElement serialize(TZRequest src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement element = delegate.toJsonTree(src);

        if (element.isJsonObject() && element.getAsJsonObject().isEmpty()) return JsonNull.INSTANCE;
        if (element.isJsonArray() && element.getAsJsonArray().isEmpty()) return JsonNull.INSTANCE;

        return element;
    }
}
