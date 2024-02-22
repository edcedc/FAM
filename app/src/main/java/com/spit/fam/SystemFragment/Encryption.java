package com.spit.fam.SystemFragment;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private static byte[] sharedkey = {
            45,68,28,38,48,58,68,78,
            70,63,57,47,97,27,19,70,
            66,68,86,87,78,39,18,80
    };

    private static byte[] sharedvector = {
            9,88,28,38,48,18,68,78
    };


    public static String encrypt(String plaintext)
            throws Exception
    {
        Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(sharedkey, "DESede"), new IvParameterSpec(sharedvector));
        byte[] encrypted = c.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt(String ciphertext)
            throws Exception
    {
        Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sharedkey, "DESede"), new IvParameterSpec(sharedvector));
        byte[] decrypted = c.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
        return new String(decrypted, "UTF-8");
    }

}
