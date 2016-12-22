package org.lazyjava.utility;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class DESProvider {
	private static final String ENCRYPTION_SCHEME = "DESede";
	private static final String UTF8 = "UTF-8";

    private KeySpec keySpec = null;
    private SecretKeyFactory keyFactory = null;
    private Cipher cipher = null;
    private String encryptionKey = "MRX3F47B-9T2487JK-WKMFRPWB";
    private SecretKey key = null;

    public DESProvider() {
        createSecretKey();
    }

    //=============================================================
    // Purpose:		set symm. key
    // Parameters: 	key-length of DES should be at least 24 bytes(192 bits)
    // Return:
    // Remark:
    // Author:		welson
    //=============================================================
    public void setKey (String _key) {
    	encryptionKey = _key;
    	createSecretKey();
    }

    public String encrypt(String plainText) throws Exception {
    	cipher.init(Cipher.ENCRYPT_MODE, key);
    	byte[] enc = cipher.doFinal(plainText.getBytes(UTF8));
    	return new String(Base64.encodeBase64(enc));
    }
    public String encrypt2HexString(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] enc = cipher.doFinal(plainText.getBytes(UTF8));
        return Hex.encodeHexString(enc);
    }


    public String decrypt(String cipherText) throws Exception {
    	cipher.init(Cipher.DECRYPT_MODE, key);
    	byte[] dec = cipher.doFinal(Base64.decodeBase64(cipherText));
    	return new String(dec);
    }
    public String decryptHex(String cipherText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] dec = cipher.doFinal(Hex.decodeHex(cipherText.toCharArray()));
        return new String(dec);
    }

    // add by welson
    private void createSecretKey() {
    	try {
    		keySpec = new DESedeKeySpec(encryptionKey.getBytes(UTF8));
	        keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_SCHEME);
	        key = keyFactory.generateSecret(keySpec);
	        cipher = Cipher.getInstance(ENCRYPTION_SCHEME);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
