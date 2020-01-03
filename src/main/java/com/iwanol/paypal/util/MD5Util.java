package com.iwanol.paypal.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

	private static MD5Util md5util;

	public static MD5Util getMD5Util() {
		if (md5util == null) {
			md5util = new MD5Util();
			return md5util;
		} else {
			return md5util;
		}
	}

	/**
	 * MD5签名
	 * 
	 * @param content
	 *            需要签名的字符串
	 * @param key
	 *            密钥
	 * @param charset
	 *            编码格式
	 * @return 签名结果
	 */
	public String sign(String content, String key, String charset) {
		content = content + key;
		return DigestUtils.md5Hex(getContentBytes(content, charset));
	}

	/**
	 * MD5签名验证
	 * 
	 * @param content
	 *            验证字符串
	 * @param sign
	 *            待验证签名
	 * @param key
	 *            密钥
	 * @param charset
	 *            编码格式
	 * @return 验证结果
	 */
	public boolean verify(String content, String sign, String key, String charset) {
		content = content + key;
		String mysign = DigestUtils.md5Hex(getContentBytes(content, charset));
		if (mysign.equals(sign)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param content
	 *            签名内容
	 * @return
	 */
	public String MD5(String content) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			String result = "";
			byte[] btInput = content.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			result = new String(str);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param content
	 * @param charset
	 * @return
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	private byte[] getContentBytes(String content, String charset) {
		if (charset == null || "".equals(charset)) {
			return content.getBytes();
		}
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
		}
	}

	/**
	 * 生成32位编码
	 * 
	 * @return string
	 */
	public String getUUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}

	public String MD5SignSimple(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuilder sign = new StringBuilder();

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] bytes = md.digest(str.getBytes("UTF-8"));

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());// 大写
		}

		return sign.toString();
	}
}
