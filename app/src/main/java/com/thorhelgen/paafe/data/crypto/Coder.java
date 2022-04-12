package com.thorhelgen.paafe.data.crypto;

import android.util.Base64;

import androidx.annotation.NonNull;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Coder {
    private static SecretKey secret;

    public static SecretKey init() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);

            secret = keyGenerator.generateKey();
            return secret;
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Exception occurred while trying to get an instance of AES encryption algorithm");
            ex.printStackTrace();
        }
        return null;
    }

    public static void init(SecretKey key) {
        secret = key;
    }

    public static String encode(@NonNull String sToE) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            return Base64.encodeToString(cipher.doFinal(sToE.getBytes()), Base64.DEFAULT);
        } catch (Exception ex) {
            System.err.println("Exception occurred during trying to encrypt string: \"" + sToE + "\"");
            ex.printStackTrace();
        }
        return null;
    }

    public static String decode(@NonNull String sToD) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secret);
            return new String(cipher.doFinal(Base64.decode(sToD, Base64.DEFAULT)));
        } catch (Exception ex) {
            System.err.println("Exception occurred during trying to decrypt string: \"" + sToD + "\"");
            ex.printStackTrace();
        }
        return null;
    }
}
