package com.iwanol.paypal.payee.daifu.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.payee.daifu.DaiFu_Payee;
import com.iwanol.paypal.util.AlgorithmUtil;
import com.iwanol.paypal.util.CertificateUtils;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.HttpSender;
import com.iwanol.paypal.util.SignUtils;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;

@Component
public class Pay_ZhangLing extends DaiFu_Payee{

	@Override
	public JSONObject pay(Payee payee, PayeeVo v) {
		// TODO Auto-generated method stub
		String url = "https://jsh.sumpay.cn/interface/deputy/zeroPay";
		//String url = "http://124.160.28.138:58082/interface/deputy/zeroPay";
		//String queryUrl = "http://101.71.243.74:58082/interface/deputy/zeroQuery";
		String queryUrl = "https://jsh.sumpay.cn/interface/deputy/zeroQuery";
		try{
			if (v.getBank_code().equals("SPDB") || v.getBank_code().equals("GDB") || v.getBank_code().equals("CITIC"))
				return Result.error.toJson("代付失败【暂不支持"+v.getBank_code()+"该银行代付】");
			Map<String,String> map = new HashMap<String,String>();
			String aesKey = AlgorithmUtil.getKey();
			map.put("service", "service.deputy.zeroPay");
			map.put("timestamp", CommonUtil.getCommonUtil().currentDateTime("yyyyMMddHHmmss",new Date()));
			map.put("mchntId",payee.getAccount());
			map.put("transAmt",String.format("%.0f", Double.valueOf(v.getTotal_amount()) * 100));
			map.put("orderNo", v.getMerchant_order());
			map.put("accountBank",AlgorithmUtil.encode(aesKey,getBank(v.getBank_code())));
			map.put("accountName", AlgorithmUtil.encode(aesKey, v.getReal_name()));
			map.put("accountNo", AlgorithmUtil.encode(aesKey, v.getBank_number()));
			map.put("aesKey",CertificateUtils.encryptByPublicKey(aesKey, payee.getPublicKey()));
			map.put("sign", SignUtils.generateSign(map, payee.getPrivateKey(), payee.getSignKey()));
			map.put("signType", "CERT");
			String param = JSONObject.toJSONString(map);
			logger.info("【请求参数:{}】",param);
			String result = HttpSender.getHttpSender().doHttpAndHttps(url, param);
			JSONObject ret = JSONObject.parseObject(result);
			logger.info("【代付结果参数:{}】",result);
			if (!StringUtils.isEmpty(result) && ret.getString("code").equals("10000")){
				String deputyJson = ret.getString("deputyJson");
				JSONObject json = JSONObject.parseObject(deputyJson);
				if (json.getString("status").equals("1") || json.getString("status").equals("2") || json.getString("status").equals("8")){
					Map<String,String> queryMap = new HashMap<String,String>();
					queryMap.put("service", "service.deputy.zeroQuery");
					queryMap.put("timestamp", CommonUtil.getCommonUtil().currentDateTime("yyyyMMddHHmmss",new Date()));
					queryMap.put("mchntId", payee.getAccount());
					queryMap.put("orderNo", v.getMerchant_order());
					queryMap.put("priKey", payee.getPrivateKey());
					queryMap.put("pubKey", payee.getSignKey());
					queryMap.put("url", queryUrl);
					new Thread(new NotifyRunnable(queryMap)).start();
					return Result.success.toJson("代付受理成功",3);
				}else{
					return Result.error.toJson("代付失败");
				}
			}
			return Result.error.toJson("代付失败");
		}catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getBank(String mark){
		if (mark.equals("COMM")){return "BCM";}
		if (mark.equals("PINGAN")){return "PAB";}
		if (mark.equals("BOB")){return "BCCB";}
		if (mark.equals("BOSC")){return "BOS";}
		if (mark.equals("BOSC")){return "BOS";}
		return mark;
	}

	@Override
	public String mark() {
		// TODO Auto-generated method stub
		return "zhangling";
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
			Map<String,String> map = new HashMap<String, String>();
			while (i < 90) {
				try{
					i++;
					String url = queryMap.get("url");
					String priKey = queryMap.get("priKey");
					String pubKey = queryMap.get("pubKey");
					logger.info("发起代付查询:{}",url);
					map.put("service", queryMap.get("service"));
					map.put("timestamp", queryMap.get("timestamp"));
					map.put("mchntId", queryMap.get("mchntId"));
					map.put("orderNo", queryMap.get("orderNo"));
					map.put("sign", SignUtils.generateSign(map, priKey, pubKey));
					map.put("signType", "CERT");
					String param = JSONObject.toJSONString(map);
					String result = HttpSender.getHttpSender().doHttpAndHttps(url, param);
					JSONObject ret = JSONObject.parseObject(result);
					if (ret.getString("code").equals("10000")){
						JSONObject json = JSONObject.parseObject(ret.getString("deputyJson"));
						if (json.getString("status").equals("2")){
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
						}else if (json.getString("status").equals("1")){
							logger.info("代付单号:{},掌灵处理中...",queryMap.get("orderNo"));
						}else if (json.getString("status").equals("8")){
							logger.info("代付单号:{},掌灵平台受理中...",queryMap.get("orderNo"));
						}else if (json.getString("status").equals("3")){
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
