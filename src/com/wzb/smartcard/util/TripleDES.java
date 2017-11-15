package com.wzb.smartcard.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

//import org.apache.commons.codec.binary.Base64;

import android.util.Log;

public class TripleDES {
	
	 private static final String ALGORITHM = "DESede";
	 
	 public static final String KEY_ALGORITHM = "DESede";  

	    //默认为 DESede/ECB/PKCS5Padding
	    private static final String CIPHER_TRANSFORMAT = "DESede/ECB/PKCS5Padding";

	    private static final String ENCODING = "UTF-8";

	    public static String encryptToBase64(String plainText, String key) throws Exception {
	    	LogUtil.logMessage("wzb", "encryptToBase64/"+"text:"+plainText+"/"+"key:"+key);
	        //SecretKey deskey = new SecretKeySpec(key.getBytes(), ALGORITHM);
	        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMAT);
	        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);  
	        SecretKey  secretKey = keyGen.generateKey();
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	        byte[] result = cipher.doFinal(plainText.getBytes(ENCODING));
	        LogUtil.logMessage("wzb", "encryptToBase64 result:"+Common.bytesToHexString(result));
	        //return Base64.encodeBase64String(result);
	        return Base64.encodeToString(result, Base64.DEFAULT);
	    }


	    public static String decryptFromBase64(String base64, String key) throws Exception {
	        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMAT);
	        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);  
	        SecretKey  secretKey = keyGen.generateKey();
	        cipher.init(Cipher.DECRYPT_MODE, secretKey);
	       // byte[] result = cipher.doFinal(Base64.decodeBase64(base64));
	        byte[] result = cipher.doFinal(Base64.decode(base64, Base64.DEFAULT));
	        return new String(result, ENCODING);
	    }

}
