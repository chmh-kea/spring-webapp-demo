package kea.techy.demo.data;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class Crypto
{
    public static byte[] hashPassword(final char[] password, final byte[] salt, final int iterations, final int keyLength)
    {
        try
        {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            byte[] res = key.getEncoded();
            return res;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] hashPassword(final char[] password, final byte[] salt)
    {
        return hashPassword(password, salt, 1, 256);
    }

    public static byte[] generateSalt()
    {
        Random random = new Random();
        int x = 32;
        byte[] salt = new byte[x];
        random.nextBytes(salt);
        return salt;
    }
}
