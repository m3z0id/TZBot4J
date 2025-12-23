package com.m3z0id.tzbot4j.network;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class AES256Factory {
    private final byte[] keyBytes;
    private final SecretKeySpec secretKey;
    private final SecureRandom rng = new SecureRandom();

    public AES256Factory(String key) throws IllegalArgumentException {
        if(key.length() != 32) throw new IllegalArgumentException("Invalid key length");
        this.keyBytes = key.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public byte[] encrypt(String toEncrypt) throws Exception {
        byte[] input = toEncrypt.getBytes(StandardCharsets.UTF_8);

        byte[] iv = new byte[16];
        rng.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(input);

        byte[] out = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(encrypted, 0, out, iv.length, encrypted.length);

        return out;
    }

    public String decrypt(byte[] encrypted) throws Exception {
        if (encrypted.length < 16) {
            throw new IllegalArgumentException("Ciphertext too short");
        }

        byte[] iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, 16);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] ciphertext = new byte[encrypted.length - 16];
        System.arraycopy(encrypted, 16, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decrypted = cipher.doFinal(ciphertext);

        try {
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decrypted data is not valid UTF‑8", e);
        }
    }
}
