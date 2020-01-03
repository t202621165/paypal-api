package com.iwanol.paypal.payee.daifu.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.payee.daifu.DaiFu_Payee;
import com.iwanol.paypal.third.zhangling.AesEncryptUtil;
import com.iwanol.paypal.third.zhangling.HttpSendUtil;
import com.iwanol.paypal.third.zhangling.SignUtil;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;
@Component
public class Pay_ZhangLing_new extends DaiFu_Payee{

	@Override
	public JSONObject pay(Payee payee, PayeeVo v) {
		// TODO Auto-generated method stub
		String url = "http://api.palmf.cn/api/tx/apply";
		String queryUrl = "http://api.palmf.cn/api/tx/query";
		String privateKey = payee.getPrivateKey();
		String key = payee.getSignKey().substring(0,16);
		try{
			if (v.getBank_code().equals("BOB") || v.getBank_code().equals("BOSC"))
				return Result.error.toJson("代付失败【暂不支持"+v.getBank_code()+"该银行代付】");
			Map<String,Object> map=new HashMap<String, Object>();
	        map.put("service","service.api.tx.apply");//接口类型
	        map.put("mchntId",payee.getAccount());//商户id  由平台分配
	        map.put("payeeName",AesEncryptUtil.aesEncrypt(v.getReal_name(), key));//收款人姓名  需要进行aes加密
	        map.put("payeeBankcardNo",AesEncryptUtil.aesEncrypt(v.getBank_number(), key));//银行卡号  需要进行aes加密
	        map.put("payeeBankName",AesEncryptUtil.aesEncrypt(getBank(v.getBank_code()), key));//银行编号  结合文档查看对应的值  需要进行aes加密
	        map.put("txAmount",String.format("%.0f", Double.valueOf(v.getTotal_amount()) * 100));//代付金额  大于1元
	        map.put("txMerNo",v.getMerchant_order());//商户代付订单号  商户自己生成
	        map.put("txAccountType","0");//账户类型  0代表对私  1代表对公
	        map.put("payeeBankCode",AesEncryptUtil.aesEncrypt(getBankCode(getBank(v.getBank_code())), key));
	        map.put("version", "v1.2");
	        String sign=SignUtil.doEncrypt(map,privateKey);//rsa签名
	        map.put("signature", sign);//签名值
	        String jsonObject= JSONObject.toJSONString(map);//最终http post请求参数 
	        logger.info("请求参数={}",jsonObject);
	        String result= HttpSendUtil.doHttpAndHttps(url, jsonObject);
	        logger.info("请求返回参数:{}",result);
	        JSONObject resp = JSONObject.parseObject(result);
	        if (resp.getString("code").equals("10000")){
	        	Map<String,String> queryMap = new HashMap<String,String>();
				queryMap.put("service", "service.api.tx.query");
				queryMap.put("mchntId", payee.getAccount());
				queryMap.put("orderNo", v.getMerchant_order());
				queryMap.put("priKey", payee.getPrivateKey());
				queryMap.put("url", queryUrl);
				new Thread(new NotifyRunnable(queryMap)).start();
	        	return Result.success.toJson("提交成功【"+resp.getString("msg")+"】",3);
	        }else{
	        	return Result.error.toJson("代付失败【"+resp.getString("msg")+"】",-3);
	        }
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Result.error.toJson("代付失败【"+e.getMessage()+"】");
		}
	}
		
	private String getBank(String mark){
		if (mark.equals("COMM")){return "BCM";}
		if (mark.equals("PINGAN")){return "PAB";}
		if (mark.equals("CITIC")){return "CNCB";}
		return mark;
	}
	
	private String getBankCode(String  payeeBankName){
		if (payeeBankName.equals("ICBC")) { return "102100099996"; }
		if (payeeBankName.equals("ABC")) { return "103100000026"; }
		if (payeeBankName.equals("BOC")) { return "104100000004"; }
		if (payeeBankName.equals("CCB")) { return "105100000017"; }
		if (payeeBankName.equals("BCM")) { return "301290000007"; }
		if (payeeBankName.equals("CMB")) { return "308584000013"; }
		if (payeeBankName.equals("GDB")) { return "306581000003"; }
		if (payeeBankName.equals("CNCB")) { return "302100011000"; }
		if (payeeBankName.equals("CMBC")) { return "305100000013"; }
		if (payeeBankName.equals("CEB")) { return "303100000006"; }
		if (payeeBankName.equals("PAB")) { return "307584007998"; }
		if (payeeBankName.equals("SPDB")) { return "310290000013"; }
		if (payeeBankName.equals("PSBC")) { return "403100000004"; }
		if (payeeBankName.equals("HXB")) { return "304100040000"; }
		if (payeeBankName.equals("CIB")) { return "309391000011"; }
		return "";
	}

	@Override
	public String mark() {
		// TODO Auto-generated method stub
		return "zhanglingnew";
	}
	
	
private class NotifyRunnable implements Runnable {
		
		private Map<String,String> queryMap;
		
		public NotifyRunnable(Map<String,String> queryMap) {
			this.queryMap = queryMap;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int i = 0;
			Map<String,Object> map = new HashMap<String, Object>();
			while (i < 90) {
				try{
					i++;
					String url = queryMap.get("url");
					String priKey = queryMap.get("priKey");
					logger.info("发起代付查询:{}",url);
					map.put("service", queryMap.get("service"));
					map.put("mchntId", queryMap.get("mchntId"));
					map.put("txMerNo", queryMap.get("orderNo"));
					map.put("sign",SignUtil.doEncrypt(map,priKey));
					String param = JSONObject.toJSONString(map);
					String result= HttpSendUtil.doHttpAndHttps(url, param);
					JSONObject ret = JSONObject.parseObject(result);
					if (ret.getString("code").equals("10000")){
						if (ret.getString("txStatus").equals("2")){
							String sql = "update settle_ment set state = ?,complate_date = ? where serial_number = ?";
							jdbcTemplate.update(sql, new PreparedStatementSetter() {							
								@Override
								public void setValues(PreparedStatement ps) throws SQLException {
									// TODO Auto-generated method stub
									ps.setInt(1, 4);
									ps.setTimestamp(2,new Timestamp(new Date().getTime()));
									ps.setString(3,queryMap.get("orderNo"));
								}
							});
							return;
						}else if (ret.getString("txStatus").equals("1")){
							logger.info("代付单号:{},银行处理中...",queryMap.get("orderNo"));
						}else if (ret.getString("txStatus").equals("0")){
							logger.info("代付单号:{},掌灵平台受理中...",queryMap.get("orderNo"));
						}else if (ret.getString("txStatus").equals("3")){
							String sql = "update settle_ment set state = ?,complate_date = ? where serial_number = ?";
							jdbcTemplate.update(sql, new PreparedStatementSetter() {							
								@Override
								public void setValues(PreparedStatement ps) throws SQLException {
									// TODO Auto-generated method stub
									ps.setInt(1, -3);
									ps.setTimestamp(2,new Timestamp(new Date().getTime()));
									ps.setString(3,queryMap.get("orderNo"));
								}
							});
							return;
						}
						if (i < 90){
							logger.info("查询掌灵代付状态第{}次",i);
							Thread.sleep(10000L);
						}else{
							String sql = "update settle_ment set state = ?,complate_date = ? where serial_number = ?";
							jdbcTemplate.update(sql, new PreparedStatementSetter() {							
								@Override
								public void setValues(PreparedStatement ps) throws SQLException {
									// TODO Auto-generated method stub
									ps.setInt(1, -3);
									ps.setTimestamp(2,new Timestamp(new Date().getTime()));
									ps.setString(3,queryMap.get("orderNo"));
								}
							});
							return;
						}
					}
				}catch (Exception e) {
					// TODO: handle exception
					logger.info(e.getMessage());
				}
			}
		}
	}

}
