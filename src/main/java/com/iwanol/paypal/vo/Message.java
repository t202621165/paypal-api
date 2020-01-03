package com.iwanol.paypal.vo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.alibaba.fastjson.JSONObject;

public enum Message {
	
	result;
	
	private String resultCode;
	
	private String resultMsg;
	
	private String url;
	
	public String ret(String status,String service) throws UnsupportedEncodingException{
		JSONObject json = new JSONObject();
		this.setResultCode(status);
		json.put("service",service);
		json.put("version","1.0");
		json.put("charset","UTF-8");
		json.put("sign_type", "MD5");
		json.put("status",this.resultCode);
		json.put("message",URLEncoder.encode(this.resultMsg,"utf-8"));
    	return json.toJSONString();
	}
	
	public String ret(String status,String service,String url) throws UnsupportedEncodingException{
		JSONObject json = new JSONObject();
		this.setResultCode(status);
		json.put("service",service);
		json.put("version","1.0");
		json.put("charset","UTF-8");
		json.put("sign_type", "MD5");
		json.put("status",this.resultCode);
		json.put("message",URLEncoder.encode(this.resultMsg,"utf-8"));
		json.put("url", url);
    	return json.toJSONString();
	}
	
	public String ret(String status) throws UnsupportedEncodingException{
		JSONObject json = new JSONObject();
		this.setResultCode(status);
		json.put("status",this.resultCode);
    	json.put("message",this.resultMsg);
    	return json.toJSONString();
	}
	
	public String msg(String status,String msg) throws UnsupportedEncodingException{
		JSONObject json = new JSONObject();
		this.setResultCode(status);
		json.put("status",this.getResultCode());
		json.put("message",msg);
    	return json.toJSONString();
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
		if("0".equals(resultCode)){
			this.resultMsg = "下单成功";
		}else if("10000".equals(resultCode)){
			this.resultMsg = "商户不存在";
		}else if ("10001".equals(resultCode)) {
			this.resultMsg = "通道暂未开通!";
		}else if ("10002".equals(resultCode)) {
			this.resultMsg = "订单号重复!";
		}else if ("10003".equals(resultCode)) {
			this.resultMsg = "系统错误!";
		}else if ("10004".equals(resultCode)) {
			this.resultMsg = "参数格式错误!";
		}else if ("10005".equals(resultCode)) {
			this.resultMsg = "点卡卡号不存在!";
		}else if ("10006".equals(resultCode)) {
			this.resultMsg = "点卡卡号或密码不正确!";
		}else if ("10007".equals(resultCode)) {
			this.resultMsg = "点卡面值不匹配!";
		}else if ("10008".equals(resultCode)) {
			this.resultMsg = "点卡已被充值!";
		}else if ("10009".equals(resultCode)) {
			this.resultMsg = "商户未绑定银行卡!";
		}else if ("10010".equals(resultCode)) {
			this.resultMsg = "商户签名认证错误!";
		}else if ("10011".equals(resultCode)) {
			this.resultMsg = "订单已被处理!";
		}else if ("10012".equals(resultCode)) {
			this.resultMsg = "下单失败!";
		}else if ("10013".equals(resultCode)) {
			this.resultMsg = "通道暂不支持二维码图片!";
		}else if ("10014".equals(resultCode)) {
			this.resultMsg = "充值失败!";
		}else if ("10015".equals(resultCode)) {
			this.resultMsg = "该通道暂不支持此点卡类型!";
		}else if ("10016".equals(resultCode)) {
			this.resultMsg = "系统关闭了该类卡支付!";
		}else if ("10017".equals(resultCode)) {
			this.resultMsg = "卡号或密码不符合规范!";
		}else if ("10018".equals(resultCode)) {
			this.resultMsg = "系统繁忙稍后重试!";
		}else if ("10019".equals(resultCode)) {
			this.resultMsg = "卡号密码已使用!";
		}else if ("10020".equals(resultCode)) {
			this.resultMsg = "卡号密码重复使用!";
		}else if ("10021".equals(resultCode)) {
			this.resultMsg = "参数为空!";
		}else if("10022".equals(resultCode)){
			this.resultMsg = "接口本版不符合要求!正确版本为1.0";
		}else if("10023".equals(resultCode)){
			this.resultMsg = "字符编码不正确,请填写UTF-8!";
		}else if("10024".equals(resultCode)){
			this.resultMsg = "签名方式不正确,请填写MD5!";
		}else if("10025".equals(resultCode)){
			this.resultMsg = "请正确填写service的值,该值不能为空!";
		}else if("10026".equals(resultCode)){
			this.resultMsg = "下单提交参数变量名错误!";
		}else if("10027".equals(resultCode)){
			this.resultMsg = "当前通道暂不支持此支付类型！";
		}else if("10028".equals(resultCode)){
			this.resultMsg = "当前通道暂不支持此银行类型！";
		}else if("10029".equals(resultCode)){
			this.resultMsg = "商户下单功能已关闭！";
		}else if("10030".equals(resultCode)){
			this.resultMsg = "下单请求路径不完整！";
		}else if("10031".equals(resultCode)){
			this.resultMsg = "调试错误，请回到请求来源地，重新发起请求";
		}else if("10032".equals(resultCode)){
			this.resultMsg = "通道二维码无法正常支付";
		}else if("10033".equals(resultCode)){
			this.resultMsg = "该订单是成功订单,补发网关积极拒绝";
		}else if("10034".equals(resultCode)){
			this.resultMsg = "未支付成功订单,补发网关积极拒绝";
		}else if("10035".equals(resultCode)){
			this.resultMsg = "订单补发成功";
		}else if("10036".equals(resultCode)){
			this.resultMsg = "订单不存在,补发网关积极拒绝";
		}else if("10037".equals(resultCode)){
			this.resultMsg = "补发失败,目标服务器积极拒绝";
		}else if("10038".equals(resultCode)){
			this.resultMsg = "下单失败,风控域名不存在";
		}else if("10039".equals(resultCode)){
			this.resultMsg = "下单失败,下单提交域名不存在";
		}else if("10040".equals(resultCode)){
			this.resultMsg = "该笔订单不存在,拒绝退款操作";
		}else if("10041".equals(resultCode)){
			this.resultMsg = "网关接收退款请求成功";
		}else if("10042".equals(resultCode)){
			this.resultMsg = "短信验证码发送成功";
		}else if("10043".equals(resultCode)){
			this.resultMsg = "短信验证码发送失败";
		}
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
