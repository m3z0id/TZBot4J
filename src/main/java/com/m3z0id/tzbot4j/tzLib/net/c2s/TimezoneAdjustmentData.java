package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.UUID;

public class TimezoneAdjustmentData implements Identifiable {
    private UUID uuid;
    private String timezone;

    @Override
    public byte getRequestId() {
        return 8;
    }

    public TimezoneAdjustmentData(@NotNull UUID uuid, @NotNull ZoneId timezone) {
        this.uuid = uuid;
        this.timezone = timezone.getId();
    }
}
