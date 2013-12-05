package com.roly.nfc.crypto.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.util.Base64;

public class EncryptionUtils {

    public static String encrypt(SecretKey key, String stringToEncrypt) throws BadPaddingException,
            IllegalBlockSizeException,
            UnsupportedEncodingException,
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException {

        Cipher ecipher = Cipher.getInstance("DES");

        ecipher.init(Cipher.ENCRYPT_MODE, key);

        // Encode the string into bytes using utf-8
        byte[] utf8 = stringToEncrypt.getBytes("UTF8");

        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);
        // Encode bytes to base64 to get a string
        return Base64.encodeToString(enc,Base64.DEFAULT);

    }

    public static String decrypt(SecretKey key, String stringToDecrypt) throws BadPaddingException,
            IllegalBlockSizeException,
            UnsupportedEncodingException,
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidKeyException {

        Cipher decipher = Cipher.getInstance("DES");

        decipher.init(Cipher.DECRYPT_MODE, key);
        // Decode base64 to get bytes
        byte[] dec = Base64.decode(stringToDecrypt.getBytes(),Base64.DEFAULT);

        // Decrypt
        byte[] utf8 = decipher.doFinal(dec);

        // Decode using utf-8
        return new String(utf8, "UTF8");

    }
}
