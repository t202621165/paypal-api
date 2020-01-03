package com.iwanol.paypal.vo;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.MD5Util;

public class PayeeVo {
	
	private String merchant_id; //商户账号
	
	private String merchant_order; //业务订单号唯一
	
	private String total_amount; //代付金额
	
	private String bank_code; //银行卡标识
	
	private String bank_name; //开户行名称
	
	private String real_name; //开户真实姓名
	
	private String bank_number; //开户行卡号
	
	private String sign; //签名结果
	
	private String cost;
	
	private String discription;
	
	private String mId;
	
	private String type = "payee";
	
	@SuppressWarnings("unchecked")
	public JSONObject valid(String key){	
		try{
			if (StringUtils.isEmpty(this.merchant_order)){
				return Result.error.toJson("订单号不能为空");
			}
			if (StringUtils.isEmpty(this.total_amount)){
				return Result.error.toJson("订单金额不能为空");
			}
			if (StringUtils.isEmpty(this.bank_code)){
				return Result.error.toJson("银行编号不能为空");
			}
			if (StringUtils.isEmpty(this.bank_name)){
				return Result.error.toJson("开户行不能为空");
			}
			if (StringUtils.isEmpty(this.real_name)){
				return Result.error.toJson("开户行姓名不能为空");
			}
			if (StringUtils.isEmpty(this.bank_number)){
				return Result.error.toJson("开户卡号不能为空");
			}		
			this.bank_name = URLDecoder.decode(this.bank_name, "UTF-8");
			this.real_name = URLDecoder.decode(this.real_name, "UTF-8");
			Set<String> noSigns = new HashSet<String>();
			noSigns.add("sign");
			noSigns.add("type");
			String str = CoreUtil.getCoreUtil().formatUrlMap(BeanMap.create(this), noSigns, true, false, true);
			if (MD5Util.getMD5Util().verify(str, this.sign, key, "utf-8")){
				return Result.success.toJson("校验成功");
			}else{
				JSONObject data = new JSONObject();
				data.put("sign", this.sign);
				return Result.error.toJson("签名校验错误【SIGN_ERROR】",data);
			}
		}catch (Exception e) {
			// TODO: handle exception
			return Result.error.toJson(e.getMessage());
		}
	}

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getMerchant_order() {
		return merchant_order;
	}

	public void setMerchant_order(String merchant_order) {
		this.merchant_order = merchant_order;
	}

	public String getTotal_amount() {
		return total_amount;
	}

	public void setTotal_amount(String total_amount) {
		this.total_amount = total_amount;
	}

	public String getBank_code() {
		return bank_code;
	}

	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_number() {
		return bank_number;
	}

	public void setBank_number(String bank_number) {
		this.bank_number = bank_number;
	}

	public String getReal_name() {
		return real_name;
	}

	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
