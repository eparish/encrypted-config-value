/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.config;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.immutables.value.Value;

@Value.Immutable
public abstract class KeyWithAlgorithm {
    abstract String getAlgorithm();

    abstract byte[] getKey();

    @Override
    public final String toString() {
        byte[] encodedKey = Base64.getEncoder().encode(getKey());
        String encodedKeyString = new String(encodedKey, StandardCharsets.UTF_8);
        return getAlgorithm() + ":" + encodedKeyString;
    }

    public final void toFile(Path path) throws IOException {
        byte[] serialized = toString().getBytes(StandardCharsets.UTF_8);
        Files.write(path, serialized, StandardOpenOption.CREATE_NEW);
    }

    static KeyWithAlgorithm from(String algorithm, byte[] key) {
        return ImmutableKeyWithAlgorithm.builder()
                .algorithm(algorithm)
                .key(key)
                .build();
    }

    static KeyWithAlgorithm randomKey(String algorithm, int size) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(size);
        SecretKey secretKey = keyGen.generateKey();
        return KeyWithAlgorithm.from(algorithm, secretKey.getEncoded());
    }

    static KeyWithAlgorithm fromString(String keyWithAlgorithm) {
        checkArgument(keyWithAlgorithm.contains(":"), "Key must be in the format <algorithm>:<key in base64>");
        String[] tokens = keyWithAlgorithm.split(":", 2);
        byte[] decodedKey = Base64.getDecoder().decode(tokens[1].getBytes(StandardCharsets.UTF_8));
        return KeyWithAlgorithm.from(tokens[0], decodedKey);
    }

    static KeyWithAlgorithm fromPath(Path keyPath) throws IOException {
        byte[] contents = Files.readAllBytes(keyPath);
        return KeyWithAlgorithm.fromString(new String(contents, StandardCharsets.UTF_8));
    }
}
