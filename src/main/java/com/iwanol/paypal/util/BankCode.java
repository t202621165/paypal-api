package com.iwanol.paypal.util;

public class BankCode {
	private static BankCode bankCode;

	public static BankCode getBankCode() {
		if (bankCode == null) {
			bankCode = new BankCode();
			return bankCode;
		} else {
			return bankCode;
		}
	}

	/** 多宝通cardno ***/
	public String dbt(String mark) {
		if ("wechat".equals(mark)) {
			return "32";
		} // 微信
		if ("qpay".equals(mark)) {
			return "36";
		} // QQ钱包
		if ("alipay".equals(mark)) {
			return "42";
		} // 支付宝
		if ("hbpay".equals(mark)) {
			return "42";
		} // 支付宝
		if ("h5_alipay".equals(mark)) {
			return "41";
		} // 支付宝WAP
		if ("h5_wechat".equals(mark)) {
			return "44";
		} // 微信WAP
		return "0";
	}
	
	public String jiabei(String mark){
		if (mark.equals("CMB")) {return "CMBCHINA";}
		if (mark.equals("PINGAN")){{return "PINGANBANK";}}
		return mark;
	}
}
