package sp.inetvpn.util;

import android.util.Base64;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    public static String Encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: " + Base64.encodeToString(encrypted, android.util.Base64.DEFAULT));

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            return value;
        }
    }

    public static String Decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

            return new String(original);
        } catch (Exception e) {
            return encrypted;
        }
    }


    public static final String initVector = "DevelopedByAmwer";
    public static final String CryptoSecretKey = "should 16 char!!";

    public static String EncryptThis(String str) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(CryptoSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(1, secretKeySpec, ivParameterSpec);
            return stringToHex(Base64.encodeToString(cipher.doFinal(str.getBytes()), 0));
        } catch (Exception e) {
//            Log.e("Cant Encrypt String", e.toString());
            return str;
        }
    }

    public static String DecryptThis(String str) {
        try {
            String hexToString = hexToString(str);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(CryptoSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, secretKeySpec, ivParameterSpec);
            return new String(cipher.doFinal(Base64.decode(hexToString, 0)));
        } catch (Exception e) {
//            Log.e("Cant Decrypt String", e.toString());
            return str;
        }
    }

    public static String stringToHex(String str) {
        return String.format("%040x", new BigInteger(1, str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String hexToString(String str) {
        String str2 = "";
        int i = 0;
        while (i < str.length()) {
            int i2 = i + 2;
            int parseInt = Integer.parseInt(str.substring(i, i2), 16);
            str2 = str2 + ((char) parseInt);
            i = i2;
        }
        return str2;
    }

}


