package com.m3z0id.tzbot4j.factory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AES256Factory {
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom rng = new SecureRandom();

    public AES256Factory(String key) {
        if (key.length() != 32) throw new IllegalArgumentException("Invalid key length");
        this.secretKey = new SecretKeySpec(key.getBytes(), "AES");
    }

    public byte[] encrypt(byte[] input) throws Exception {
        return encrypt(input, null);
    }

    public byte[] encrypt(byte[] input, byte[] additional) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        rng.nextBytes(iv);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        if (additional != null) cipher.updateAAD(additional);

        byte[] ciphertext = cipher.doFinal(input);

        byte[] out = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);

        return out;
    }

    public byte[] decrypt(byte[] encrypted) throws Exception {
        return decrypt(encrypted, null);
    }

    public byte[] decrypt(byte[] encrypted, byte[] additional) throws Exception {
        if (encrypted.length < IV_LENGTH) throw new IllegalArgumentException("Ciphertext too short");

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encrypted, 0, iv, 0, IV_LENGTH);

        byte[] ciphertext = new byte[encrypted.length - IV_LENGTH];
        System.arraycopy(encrypted, IV_LENGTH, ciphertext, 0, ciphertext.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        if (additional != null) cipher.updateAAD(additional);

        return cipher.doFinal(ciphertext);
    }
}

