package com.iwanol.paypal.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 加密工具类
 * 
 * @author iwanol
 *
 */
public class Encryption {
	private static Encryption encryption;

	public static Encryption getEncryption() {
		if (encryption == null) {
			encryption = new Encryption();
			return encryption;
		} else {
			return encryption;
		}
	}

	/**
	 * 对明文进行加密
	 * 
	 */
	private final String key = "FFF52F07C0357E245F9681B8775EC12C";

	public String aesEncrypt(String content) throws Exception {
		byte[] byteRe = aesEncry(content, key);
		String encrytStr = aesParseByte2HexStr(byteRe);
		return encrytStr;
	}

	/**
	 * 对加密数据进行校验 (true为校验成功，false为校验失败)
	 * 
	 * @throws Exception
	 */
	public boolean aesVerify(String content) throws Exception {
		boolean flag = false;
		byte[] byteRe = aesEncry(content, key);
		String encrytStr = aesParseByte2HexStr(byteRe);
		if (encrytStr.equals(content)) {
			flag = true;
		}
		return flag;
	}

	/**
	 * 对密文进行解密
	 * 
	 * @param hexStr
	 * @return
	 * @throws Exception
	 */
	public String aesDeCrypt(String enCryptContent) throws Exception {
		byte[] encrytByte;
		encrytByte = aesParseHexStr2Byte(enCryptContent);
		String content = aesDeCrypt(encrytByte, key);
		return content;
	}

	public byte[] aesParseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public String aesParseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	public String aesDeCrypt(byte[] src, String strKey) throws Exception {
		KeyGenerator keygen;
		SecretKey desKey;
		Cipher c;
		byte[] cByte;

		keygen = KeyGenerator.getInstance("AES");
		keygen.init(128, new SecureRandom(strKey.getBytes()));

		desKey = keygen.generateKey();
		c = Cipher.getInstance("AES");

		c.init(Cipher.DECRYPT_MODE, desKey);

		cByte = c.doFinal(src);

		return new String(cByte, "UTF-8");
	}

	public byte[] aesEncry(String content, String strKey) throws Exception {
		KeyGenerator keygen;
		SecretKey desKey;
		Cipher c;
		byte[] cByte;
		String str = content;

		keygen = KeyGenerator.getInstance("AES");
		keygen.init(128, new SecureRandom(strKey.getBytes()));

		desKey = keygen.generateKey();
		c = Cipher.getInstance("AES");

		c.init(Cipher.ENCRYPT_MODE, desKey);

		cByte = c.doFinal(str.getBytes("UTF-8"));

		return cByte;
	}
}
