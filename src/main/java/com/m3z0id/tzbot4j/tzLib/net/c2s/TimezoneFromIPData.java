package com.m3z0id.tzbot4j.tzLib.net.c2s;

import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class TimezoneFromIPData implements Identifiable {
    private int ip;
    public TimezoneFromIPData(@NotNull String ip) throws UnknownHostException {
        Inet4Address address = (Inet4Address) Inet4Address.getByAddress(ip.getBytes(StandardCharsets.UTF_8));

        this.ip = 0;
        for (byte b: address.getAddress()) {
            this.ip = this.ip << 8 | (b & 0xFF);
        }
    }

    @Override
    public byte getRequestId() {
        return 2;
    }
}
