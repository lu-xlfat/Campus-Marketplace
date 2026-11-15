
package campusmarketplace.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERATIONS = 600000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {}

    public static String hashPassword(char[] password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hash = deriveKey(password, salt, ITERATIONS);

        return "pbkdf2_sha256:" + ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(
            char[] password, String storedHash) {

        try {
            String[] parts = storedHash.split(":");

            if (parts.length != 4
                    || !parts[0].equals("pbkdf2_sha256")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations < 10000 || iterations > 2000000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            byte[] actual = deriveKey(password, salt, iterations);

            return java.security.MessageDigest.isEqual(
                    expected, actual);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] password, byte[] salt, int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, KEY_LENGTH);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }
}

