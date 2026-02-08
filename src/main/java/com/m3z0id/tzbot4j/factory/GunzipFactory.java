package com.m3z0id.tzbot4j.factory;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GunzipFactory {
    public GunzipFactory() {}
    public @Nullable byte[] compress(byte[] msg) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(output);
        gzip.write(msg);
        gzip.close();
        return output.toByteArray();
    }
    public @Nullable byte[] decompress(byte[] msg) {
        try {
            return new GZIPInputStream(new ByteArrayInputStream(msg)).readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
