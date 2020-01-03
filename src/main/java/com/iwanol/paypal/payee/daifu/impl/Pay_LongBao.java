package com.iwanol.paypal.payee.daifu.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.payee.daifu.DaiFu_Payee;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;

@Component
public class Pay_LongBao extends DaiFu_Payee{

	@Override
	public JSONObject pay(Payee payee, PayeeVo v) {
		// TODO Auto-generated method stub
		String url = "https://gate.longpay.com/agentpay/index";
		StringBuffer buffer = new StringBuffer();
		Map<String,String> map = new HashMap<String,String>();
		map.put("parter",payee.getAccount());
		map.put("orderid", v.getMerchant_order());
		map.put("amount", String.format("%.2f",Double.valueOf(v.getTotal_amount())));
		map.put("account_no", v.getBank_number());
		map.put("account_name", v.getReal_name());
		map.put("bank_code", v.getBank_code());
		map.put("nonce_str", UUID.fastUUID().toString().replace("-",""));
		map.put("clientip", "116.62.111.54");
		map.put("key", payee.getSignKey());
		buffer.append("parter="+map.get("parter")).append("&orderid="+map.get("orderid")).append("&accout_type=0").append("&amount="+map.get("amount"))
		.append("&account_no="+map.get("account_no")).append("&account_name="+map.get("account_name")).append("&bank_code="+map.get("bank_code"))
		.append("&nonce_str="+map.get("nonce_str")).append("&clientip="+map.get("clientip")).append("&sign="+this.sign(map));
		
		logger.info("龙宝代付请求报文串【{}】",buffer.toString());
		try{
			String result = HttpRequest.post(url).body(buffer.toString().getBytes("GB2312")).charset("GB2312").execute().body();
			logger.info("龙宝代付响应参数:{}",result);
			if (!StringUtils.isEmpty(result)){
				JSONObject resp = JSONObject.parseObject(result);
				if (resp.getString("code").equals("0000")){
					return Result.success.toJson("龙宝代付受理成功,等待龙宝出款");		
				}else{
					return Result.error.toJson("龙宝代付失败【"+resp.getString("msg")+"】");
				}
			}
			return null;
		}catch (Exception e) {
			// TODO: handle exception
			return Result.error.toJson("代付失败【"+e.getMessage()+"】");
		}
	}
	
	private String sign(Map<String,String> map){
		StringBuffer buffer = new StringBuffer();
		buffer.append("parter="+map.get("parent")).append("&orderid="+map.get("orderid"))
		.append("&amount="+ map.get("amount")).append("&accout_type=0")
		.append("&account_no="+map.get("account_no")).append("&account_name="+map.get("account_name"))
		.append("&nonce_str="+map.get("nonce_str"));
		logger.info(String.format("请求签名串：%s",buffer.toString())+map.get("key"));
		return MD5Util.getMD5Util().sign(buffer.toString(), map.get("key"), "GB2312");
	}

	@Override
	public String mark() {
		// TODO Auto-generated method stub
		return "longbao";
	}

}
