package com.m3z0id.tzbot4j.factory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class ChaCha20Factory {
    private static final int NONCE_LENGTH = 12;

    private final SecretKeySpec secretKey;
    private final SecureRandom rng = new SecureRandom();

    public ChaCha20Factory(String key) {
        if (key.length() != 32)  throw new IllegalArgumentException("Invalid key length");
        this.secretKey = new SecretKeySpec(key.getBytes(), "ChaCha20");
    }

    public byte[] encrypt(byte[] input) throws Exception {
        return encrypt(input, null);
    }

    public byte[] encrypt(byte[] input, byte[] aad) throws Exception {
        byte[] nonce = new byte[NONCE_LENGTH];
        rng.nextBytes(nonce);

        IvParameterSpec ivSpec = new IvParameterSpec(nonce);

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        if (aad != null) cipher.updateAAD(aad);

        byte[] ciphertext = cipher.doFinal(input);

        byte[] out = new byte[nonce.length + ciphertext.length];
        System.arraycopy(nonce, 0, out, 0, nonce.length);
        System.arraycopy(ciphertext, 0, out, nonce.length, ciphertext.length);
        return out;
    }

    public byte[] decrypt(byte[] encrypted) throws Exception {
        return decrypt(encrypted, null);
    }

    public byte[] decrypt(byte[] encrypted, byte[] aad) throws Exception {
        if (encrypted.length < NONCE_LENGTH) throw new IllegalArgumentException("Ciphertext too short");

        byte[] nonce = new byte[NONCE_LENGTH];
        System.arraycopy(encrypted, 0, nonce, 0, NONCE_LENGTH);

        byte[] ciphertext = new byte[encrypted.length - NONCE_LENGTH];
        System.arraycopy(encrypted, NONCE_LENGTH, ciphertext, 0, ciphertext.length);

        IvParameterSpec ivSpec = new IvParameterSpec(nonce);

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        if (aad != null) cipher.updateAAD(aad);
        return cipher.doFinal(ciphertext);
    }
}

