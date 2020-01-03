package com.iwanol.paypal.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HmacUtil {
	private String encodingCharset = "UTF-8";
	private static HmacUtil hmacUtil;

	public static HmacUtil getHmacUtil() {
		if (hmacUtil == null) {
			return new HmacUtil();
		}
		return hmacUtil;
	}

	/**
	 * @param content
	 *            验签字符串
	 * @param key
	 *            密钥
	 * @return
	 */
	public String hmacSign(String content, String key) {
		byte k_ipad[] = new byte[64];
		byte k_opad[] = new byte[64];
		byte keyb[];
		byte value[];
		try {
			keyb = key.getBytes(encodingCharset);
			value = content.getBytes(encodingCharset);
		} catch (UnsupportedEncodingException e) {
			keyb = key.getBytes();
			value = content.getBytes();
		}
		Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
		Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
		for (int i = 0; i < keyb.length; i++) {
			k_ipad[i] = (byte) (keyb[i] ^ 0x36);
			k_opad[i] = (byte) (keyb[i] ^ 0x5c);
		}

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {

			return null;
		}
		md.update(k_ipad);
		md.update(value);
		byte dg[] = md.digest();
		md.reset();
		md.update(k_opad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return toHex(dg);
	}

	/**
	 * hmac签名验证
	 * 
	 * @param sign
	 *            待验证内容
	 * @param content
	 *            待加密串
	 * @param key
	 *            密钥
	 * @return
	 */
	public boolean hmacVerify(String sign, String content, String key) {
		String hmac = hmacSign(content, key);
		if (content.equals(hmac)) {
			return true;
		} else {
			return false;
		}
	}

	public String toHex(byte input[]) {
		if (input == null)
			return null;
		StringBuffer output = new StringBuffer(input.length * 2);
		for (int i = 0; i < input.length; i++) {
			int current = input[i] & 0xff;
			if (current < 16)
				output.append("0");
			output.append(Integer.toString(current, 16));
		}

		return output.toString();
	}
}
