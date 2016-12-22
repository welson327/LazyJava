package org.lazyjava.utility;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class AESProvider {
	private static final String ENCRYPTION_SCHEME = "AES";
	private static final String UTF8 = "UTF-8";

    private SecretKeySpec keySpec = null;
    //private SecretKeyFactory keyFactory = null;
    private Cipher cipher = null;
    private String encryptionKey = null;
    private SecretKey key = null;

    public AESProvider() {
        createSecretKey();
    }

    //=============================================================
    // Purpose:		set symm. key
    // Parameters: 	key-length of AES should be 16 bytes(128 bits)
    // Return:
    // Remark:
    // Author:		welson
    //=============================================================
    public void setKey (String _key) {
    	encryptionKey = _key;
    	createSecretKey();
    }

    public String encrypt(String plainText) throws Exception {
    	cipher.init(Cipher.ENCRYPT_MODE, keySpec);
    	byte[] enc = cipher.doFinal(plainText.getBytes(UTF8));
    	return new String(Base64.encodeBase64(enc));
    }
    public String encrypt2HexString(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] enc = cipher.doFinal(plainText.getBytes(UTF8));
        return Hex.encodeHexString(enc);
    }


    public String decrypt(String cipherText) throws Exception {
    	cipher.init(Cipher.DECRYPT_MODE, keySpec);
    	byte[] dec = cipher.doFinal(Base64.decodeBase64(cipherText));
    	return new String(dec);
    }
    public String decryptHex(String cipherText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] dec = cipher.doFinal(Hex.decodeHex(cipherText.toCharArray()));
        return new String(dec);
    }

    // add by welson
    private void createSecretKey() {
    	try {
    		if(encryptionKey == null) {
	    		KeyGenerator keyGen = KeyGenerator.getInstance(ENCRYPTION_SCHEME);
	    		keyGen.init(128);
	    		key = keyGen.generateKey();
	    		byte[] binaryKey = key.getEncoded();
	    		keySpec = new SecretKeySpec(binaryKey, ENCRYPTION_SCHEME);
	    		cipher = Cipher.getInstance(ENCRYPTION_SCHEME);
    		} else {
	    		keySpec = new SecretKeySpec(encryptionKey.getBytes(UTF8), ENCRYPTION_SCHEME);
		        //keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_SCHEME);
		        //key = keyFactory.generateSecret(keySpec);
		        cipher = Cipher.getInstance(ENCRYPTION_SCHEME);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
