package org.imixs.office.ejb.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encrypter using the SHA-256 algorithm 
 * @see http://www.mkyong.com/java/java-sha-hashing-example/
 * @author rsoika
 *
 */
public class Crypter {
    private static MessageDigest digester;

    static {
        try {
        	// use MD5 or SHA-256
            digester = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String crypt(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }

        digester.update(str.getBytes());
        
        byte byteData[] = digester.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        return sb.toString();
    }
}
