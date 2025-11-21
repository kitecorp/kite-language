package io.kite.stdlib.functions.utility;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;

public class HashFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 1 || args.size() > 2) {
            throw new RuntimeException(MessageFormat.format("Expected 1 or 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String input)) {
            throw new RuntimeException("First argument must be a string");
        }

        String algorithm = args.size() == 2 ? args.get(1).toString() : "SHA-256";

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported hash algorithm: " + algorithm);
        }
    }
}
