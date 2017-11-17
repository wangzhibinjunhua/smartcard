package com.wzb.smartcard.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

//import org.apache.commons.codec.binary.Base64;

import android.util.Log;

public class TripleDES {

	private static final String ALGORITHM = "DESede";

	public static final String KEY_ALGORITHM = "DESede";

	// 默认为 DESede/ECB/PKCS5Padding
	private static final String CIPHER_TRANSFORMAT = "DESede/ECB/PKCS5Padding";

	private static final String ENCODING = "UTF-8";

	public static String encodeToHexStr(String data, byte[] customkey) {
		
		Key key = null;
		try {
			DESKeySpec keySpec = new DESKeySpec(customkey);// 设置密钥参数
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");// 获得密钥工厂
			key = keyFactory.generateSecret(keySpec);// 得到密钥对象

			Cipher enCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");// 得到加密对象Cipher
			enCipher.init(Cipher.ENCRYPT_MODE, key);// 设置工作模式为加密模式，给出密钥和向量
			byte[] pasByte = enCipher.doFinal(Common.parseHexStringToBytes(data));
			LogUtil.logMessage("wzb", "result:"+Common.bytesToHexString(pasByte));
			
			return Convert.bytesToHexString(pasByte, 0, Common.parseHexStringToBytes(data).length);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} 
	}

	public static String encryptToBase64(String plainText, byte[] key) throws Exception {
		LogUtil.logMessage("wzb",
				"encryptToBase64/" + "text:" + plainText + "/" + "key:" + Common.bytesToHexString(key));
		// SecretKey deskey = new SecretKeySpec(key.getBytes(), ALGORITHM);
		LogUtil.logMessage("wzb", "key bytes len:" + key.length);
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMAT);
		SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		byte[] result = cipher.doFinal(Common.parseHexStringToBytes(plainText));
		LogUtil.logMessage("wzb", "encryptToBase64 result:" + Common.bytesToHexString(result));
		// return Base64.encodeBase64String(result);
		return Base64.encodeToString(result, Base64.DEFAULT);
	}

	public static String decryptFromBase64(String base64, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMAT);
		KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
		SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		// byte[] result = cipher.doFinal(Base64.decodeBase64(base64));
		byte[] result = cipher.doFinal(Base64.decode(base64, Base64.DEFAULT));
		return new String(result, ENCODING);
	}

}
