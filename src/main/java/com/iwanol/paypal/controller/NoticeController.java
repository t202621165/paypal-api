package com.iwanol.paypal.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.service.impl.BankServiceImpl;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.service.impl.SettleMentServiceImpl;
import com.iwanol.paypal.third.hangtian.MySecurity;
import com.iwanol.paypal.third.hangtian.PPSecurity;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.ConnectionUrl;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.HmacUtil;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.util.XmlUtil;
import com.iwanol.paypal.vo.Message;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class NoticeController {
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	@Autowired
	private ApiController apiController;
	@Autowired
	private BankServiceImpl bankServiceImpl;
	
	@Autowired
	private SettleMentServiceImpl settleMentServiceImpl;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 支付宝官方异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/zfb_notify")
	public void zfbNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		//获取post的参数
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("【支付宝官方异步通知爱玩参数:"+params.toString()+"】");
		Set<String> keySets = new HashSet<String>();
		String sysOrderNumber = request.getParameter("out_trade_no"); //系统订单号
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String sign = params.get("sign").toString();
		keySets.add("sign");
		keySets.add("sign_type");
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(params,keySets,true,false,false);
		if(MD5Util.getMD5Util().verify(signStr, sign,platformOrder.getGallery().getGalleryMD5Key(),"UTF-8")){
			if (params.get("trade_status").equals("TRADE_SUCCESS")){
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("trade_no").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"success", response);
				}else{
					//订单已处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				//返回成功信息给支付宝
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 微信官方异步通知
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	@RequestMapping("/wechat_notify")
	public void wechatNotify(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String xml = CoreUtil.getCoreUtil().getXmlParams(request);
		if (!StringUtils.isEmpty(xml)){
			Set<String> sets = new HashSet<String>();
			sets.add("sign");
			Map<String, String> data = XmlUtil.toMap(xml.getBytes(), "utf-8");
			String sysOrderNumber = data.get("out_trade_no"); //系统订单号
			PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
			String sign = data.get("sign").toString();
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(data,sets, true, false, true);
			if (data.get("result_code").equals("SUCCESS")){
				if (MD5Util.getMD5Util().verify(signStr, sign.toLowerCase(), "&key="+platformOrder.getGallery().getGalleryMD5Key(), "utf-8")){
					if (platformOrder.getState() == 0){
						platformOrder.setPartyOrderNumber(data.get("transaction_id")); //第三方订单号
						apiController.sendNotifyParam(platformOrder,"success", response);
					}else{
						response.getWriter().print("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
						ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
					}
				}else{
					response.getWriter().println("fail");
				}
			}else{
				response.getWriter().println("fail");
			}
			
		}else{
			response.getWriter().println("fail");
		}
	}
	
	/**
	 * 威富通异步通知
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	@RequestMapping("/swift_notify")
	public void swiftNotify(HttpServletRequest request,HttpServletResponse response) throws Exception{
		String xml = CoreUtil.getCoreUtil().getXmlParams(request);
		if (!StringUtils.isEmpty(xml)){
			Set<String> sets = new HashSet<String>();
			sets.add("sign");
			Map<String, String> data = XmlUtil.toMap(xml.getBytes(), "utf-8");
			String sysOrderNumber = data.get("out_trade_no"); //系统订单号
			PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
			String sign = data.get("sign").toString();
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(data,sets, true, false, true);
			if (data.get("status").equals("0")){
				if (MD5Util.getMD5Util().verify(signStr, sign.toLowerCase(), "&key="+platformOrder.getGallery().getGalleryMD5Key(), "utf-8")){
					if (platformOrder.getState() == 0){
						platformOrder.setPartyOrderNumber(data.get("transaction_id")); //第三方订单号
						apiController.sendNotifyParam(platformOrder,"success", response);
					}else{
						response.getWriter().print("success");
						ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
					}
				}else{
					response.getWriter().println("fail");
				}
			}else{
				response.getWriter().println("fail");
			}
			
		}else{
			response.getWriter().println("fail");
		}
	}
		
	/**
	 * 多宝通异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/51upay_notify")
	public void dbtNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		//获取post的参数
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("【多宝通异步通知爱玩参数:"+params.toString()+"】");
		String sysOrderNumber = request.getParameter("sdcustomno"); //系统订单号
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String reSign = params.get("resign").toString();
		String resignStr = CoreUtil.getCoreUtil().formatUrlMapToTy("51PayZfb","notify_resign",params);
		if(MD5Util.getMD5Util().verify(resignStr, reSign.toLowerCase(), "&key="+platformOrder.getGallery().getGalleryMD5Key(),"UTF-8")){//验签通过
			if (params.get("state").equals("1")){//充值成功
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("sd51no").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"<result>1</result>", response);
				}else{
					//订单已被处理
					response.getWriter().print("<result>1</result>");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				//返回成功信息给51
				response.getWriter().print("<result>1</result>");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("<result>0</result>");
		}
	}
	
	/**
	 * 优易数卡异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/ue_notify")
	public void ueNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		String sysOrderNumber = params.get("r6_Order").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String hmacStr = CoreUtil.getCoreUtil().formatUrlMapToTy("186ue","notify1",params);
		if(HmacUtil.getHmacUtil().hmacVerify(params.get("hmac").toString(),hmacStr,platformOrder.getGallery().getGalleryMD5Key())){
			if(params.get("r1_Code").equals("1")){ //支付成功
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("r2_TrxId").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"success", response);
				}else{
					//订单已被处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				//返回成功信息给51
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 双乾异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/shuangqian_notify")
	public void shuangqianNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		String sysOrderNumber = params.get("BillNo").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String sign = MD5Util.getMD5Util().sign(CoreUtil.getCoreUtil().formatUrlMapToTy("shuangqian", "notify", params), "&"+MD5Util.getMD5Util().MD5(platformOrder.getGallery().getGalleryMD5Key()).toUpperCase(), "UTF-8").toUpperCase();
		if (sign.equals(params.get("MD5info"))){
			if(params.get("Succeed").equals("88")){ //支付成功
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("Orderno").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"SUCCESS", response);
				}else{
					//订单已被处理
					response.getWriter().print("SUCCESS");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("SUCCESS");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 嘉贝异步通知
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/jiabei_notify")
	public void jiabeiNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("嘉贝接口异步通知参数:{}",JSONObject.toJSONString(params));
		String sysOrderNumber = params.get("TradeNum").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String sign = params.get("Sign").toString();
		if (params.get("RespCode").equals("1111") && params.get("Status").equals("01")){
			Set<String> noSigns = new HashSet<String>();
			noSigns.add("Sign");
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(params, noSigns, true, false, true);
			if (MD5Util.getMD5Util().verify(signStr, sign, "&key=".concat(platformOrder.getGallery().getGalleryMD5Key()), "UTF-8")){
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("OrderNum").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"SUCCESS", response);
				}else{
					//订单已被处理
					response.getWriter().print("SUCCESS");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("fail");
			}
		}else{
			response.getWriter().print("fail");
			ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
		}
	}
	
	/**
	 * 龙宝异步通知
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/longbao_notify")
	public void longBaoNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("龙宝接口异步通知参数:{}",JSONObject.toJSONString(params));
		String sysOrderNumber = params.get("orderid").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		StringBuffer buffer = new StringBuffer();
		buffer.append("orderid="+params.get("orderid")).append("&opstate="+params.get("opstate")).append("&ovalue="+String.format("%.2f",Double.valueOf(params.get("ovalue").toString())));
		if (params.get("opstate").equals("0")){
			if (MD5Util.getMD5Util().verify(buffer.toString(),String.valueOf(params.get("sign")), platformOrder.getGallery().getGalleryMD5Key(), "GB2312")){
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("sysorderid").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"opstate=0", response);
				}else{
					//订单已被处理
					response.getWriter().print("opstate=0");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("fail");
			}
		}else{
			response.getWriter().print("opstate=0");
			ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
		}
	}
	
	@RequestMapping("/hangtian_notify")
	public void hangtianNotify(HttpServletRequest request,HttpServletResponse response) throws Exception{
		Map<String,String> resp = new HashMap<String, String>();
		String transData = request.getParameter("transdata");
		logger.info("航天异步通知数据:{}",transData);
		PPSecurity ppSecurity = MySecurity.getPPSecurity();
		if(transData != null && transData.startsWith("<?xml")) {
			//说明返回的数据未加密(商户号上送不对时会出现此情况)
			logger.info("航天商户号上送不对");
		}else if(transData != null){
			byte[] resB = ppSecurity.PPFCommDataDecode(transData.getBytes("gbk"));
			logger.info("航天解密后的通知数据:{}",new String(resB,"gbk"));
			resp = XmlUtil.toMap(resB, "gbk");
			logger.info("航天xml转map:{}",new String(resB,"gbk"));
		}
		if (!StringUtils.isEmpty(resp)){
			//商户订单号
    		String merOrderNo = resp.get("merorderno");
    		//三方订单号
    		String sysorderid = resp.get("orderno");
    		//订单状态 0等待支付 1支付成功 2支付失败
    		String orderStatus = resp.get("orderstatus");
    		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(merOrderNo);
    		if (orderStatus.equals("1")){
    			if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(sysorderid); //第三方订单号
					String repData = new String(ppSecurity.PPFCommDataEncode("success".getBytes("gbk")));
					apiController.sendNotifyParam(platformOrder,repData, response);
				}else{
					//订单已被处理
					String repData = new String(ppSecurity.PPFCommDataEncode("success".getBytes("gbk")));
					response.setContentLength(repData.getBytes("utf-8").length);
					response.getOutputStream().write(repData.getBytes("utf-8"));
					response.getOutputStream().close();
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
    		}else{
    			String repData = new String(ppSecurity.PPFCommDataEncode("fail".getBytes("gbk")));
				response.setContentLength(repData.getBytes("utf-8").length);
				response.getOutputStream().write(repData.getBytes("utf-8"));
				response.getOutputStream().close();
    			ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
    		}
		}else{
			logger.info("航天xml转map失败");
		}
	}
	
	/**
	 * 万像异步通知
	 * @param request
	 * @param response
	 * @throws IOException
	 * 
	 */
	@RequestMapping("/wanxiang_notify")
	public void wanxiangNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("万像接口异步通知参数:{}",JSONObject.toJSONString(params));
		String sysOrderNumber = params.get("orderno").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		StringBuffer buffer = new StringBuffer();
		buffer.append("version="+params.get("version")).append("&status="+params.get("status")).append("&parter="+params.get("parter"))
		.append("&orderno="+params.get("orderno")).append("&amount="+params.get("amount"));
		logger.info("万像SIGN{},异步通知签名串:{}",params.get("sign"),buffer.toString());
		if(params.get("status").equals("success")){ //支付成功
			if (MD5Util.getMD5Util().verify(buffer.toString(),String.valueOf(params.get("sign")), "&key="+platformOrder.getGallery().getGalleryMD5Key(), "utf-8")){
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("orderid").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"ok", response);
				}else{
					//订单已被处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("fail");
			}	
		}else{
			response.getWriter().print("success");
			ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
		}
	}
	
	/**
	 * 汇聚异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/huiju_notify")
	public void huijuNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("汇聚接口异步通知参数:{}",JSONObject.toJSONString(params));
		String sysOrderNumber = params.get("r2_OrderNo").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		if(params.get("r6_Status").equals("100")){ //支付成功
			if(platformOrder.getState() == 0){
				platformOrder.setPartyOrderNumber(params.get("r7_TrxNo").toString()); //第三方订单号
				apiController.sendNotifyParam(platformOrder,"success", response);
			}else{
				//订单已被处理
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("success");
			ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
		}
	}
	
	@RequestMapping("/huiju_ebank_ret")
	public void huijuEbankRet(HttpServletResponse response) throws IOException{
		response.setHeader("content-type", "text/html;charset=UTF-8");
		response.setCharacterEncoding("GBK");
		response.getWriter().print("<h1 style='color:green;margin-top:20%;margin-left:35%'>您已经支付成功(√),请查看相应物品是否到账!</h1>");
	}
	
	/**
	 * 掌灵异步通知
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/zhangling_notify")
	public void zhanglingNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Set<String> keys = new HashSet<String>();
		String param = CoreUtil.getCoreUtil().getXmlParams(request);
		logger.info("掌灵异步通知接口异步通知参数:{}",param);
		JSONObject params = JSONObject.parseObject(param);
		String sysOrderNumber = params.get("mchntOrderNo").toString();
		String sign = params.getString("signature");
		keys.add("signature");
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		if (MD5Util.getMD5Util().verify(CoreUtil.getCoreUtil().formatUrlMap(params, keys, true, false, true), sign, "&key="+platformOrder.getGallery().getGalleryMD5Key(), "UTF-8")){
			if (params.getString("paySt").equals("2")){
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("outTransactionId").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"{\"success\":\"true\"}", response);
				}else{
					//订单已被处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			logger.info("掌灵异步通知签名验证不通过");
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 银联商务异步通知
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/union_notify")
	public void unionNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Set<String> keys = new HashSet<String>();
		String param = CoreUtil.getCoreUtil().getXmlParams(request);
		logger.info("银联商务接口异步通知参数:{}",param);
		JSONObject params = JSONObject.parseObject(param);
		String sysOrderNumber = params.get("billNo").toString();
		String sign = params.getString("sign");
		keys.add("sign");
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		if (MD5Util.getMD5Util().verify(CoreUtil.getCoreUtil().formatUrlMap(params, keys, true, false, true), sign, platformOrder.getGallery().getGalleryMD5Key(), "UTF-8")){
			JSONObject billPayment = JSONObject.parseObject(params.getString("billPayment"));
			if (billPayment.getString("status").equals("TRADE_SUCCESS")){				
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(billPayment.getString("targetOrderId")); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"SUCCESS", response);
				}else{
					//订单已被处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 双乾网银异步通知
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/shuangqian_ebank_notify")
	public void shuangqianEbankNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		String sysOrderNumber = params.get("BillNo").toString();
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(sysOrderNumber);
		String signStr = CoreUtil.getCoreUtil().formatUrlMapToTy("shuangqian", "notify", params);
		String sign = MD5Util.getMD5Util().sign(signStr, "&"+MD5Util.getMD5Util().MD5(platformOrder.getGallery().getGalleryMD5Key()).toUpperCase(), "UTF-8").toUpperCase();
		if (sign.equals(params.get("MD5info"))){
			if(params.get("Succeed").equals("88")){ //支付成功
				if(platformOrder.getState() == 0){
					platformOrder.setPartyOrderNumber(params.get("Orderno").toString()); //第三方订单号
					apiController.sendNotifyParam(platformOrder,"success", response);
				}else{
					//订单已被处理
					response.getWriter().print("success");
					ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10011", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
				}
			}else{
				response.getWriter().print("success");
				ConnectionUrl.getConnectionUrl().httpRequest(platformOrder.getMerchant().getNotifyUrl(),Message.result.ret("10014", CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark())));
			}
		}else{
			response.getWriter().print("fail");
		}
	}
	
	@RequestMapping("/shuangqian_ebank_ret")
	public String shuangqianEbankRet(HttpServletRequest request,RedirectAttributes attr){
		Map<String,Object> params = CoreUtil.getCoreUtil().getParams(request);
		PlatformOrder platformOrder = orderServiceImpl.findSelectivBySysOrder(params.get("BillNo").toString());
		attr.addFlashAttribute("platformOrder", platformOrder);
		return "redirect:/success.html";
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException 
	 */
	@RequestMapping("/refund")
	public void refundNotify(HttpServletRequest request,HttpServletResponse response) throws IOException{		
		Map<String, Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("【支付宝退款异步通知爱玩参数:"+params.toString()+"】");
		String result_details = URLDecoder.decode(params.get("result_details").toString(),"utf-8");
		String[] args = result_details.split("\\^");
		logger.info("【交易号:"+args[0]+"】");
		PlatformOrder platformOrder = orderServiceImpl.findByPartyOrderNumber(args[0]);
		Set<String> removeKeys = new HashSet<String>();
		removeKeys.add("sign");
		removeKeys.add("sign_type");
		params.put("notify_time", URLDecoder.decode(params.get("notify_time").toString(), "utf-8"));
		params.put("result_details", URLDecoder.decode(params.get("result_details").toString(), "utf-8"));
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(params, removeKeys, true, false, true);
		if (MD5Util.getMD5Util().verify(signStr, params.get("sign").toString(),
				platformOrder.getGallery().getGalleryMD5Key(), "utf-8")) {
			if (args[2].equals("SUCCESS")) {
				Integer state = platformOrder.getState();
				if (state == 4) {
					response.getWriter().print("success"); // 退款已被处理
				} else {
					if (state == 3) {
						if(platformOrder.getMerchantProfits().compareTo(BigDecimal.valueOf(0.00)) == 1){
							// 扣除商户该笔订单利润
							Bank bank = bankServiceImpl.findByMerchantIdAndBankType(platformOrder.getMerchant().getId(),
									true);
							bank.setOverMoney(bank.getOverMoney().subtract(platformOrder.getMerchantProfits()));
							bank.setAllDeposit(bank.getAllDeposit().subtract(platformOrder.getMerchantProfits()));
							bankServiceImpl.updateEntity(bank);
							logger.info("【订单号:"+platformOrder.getPartyOrderNumber()+"退款成功 ->从余额减去 "+platformOrder.getMerchantProfits()+" 元】");
						}
						platformOrder.setMerchantProfits(BigDecimal.valueOf(0.00));
						platformOrder.setPlatformProfits(BigDecimal.valueOf(0.00));
						platformOrder.setState(4);
					} 
					orderServiceImpl.updateEntity(platformOrder);	
				}
				response.getWriter().print("success");
			}else {
				//退款失败
				platformOrder.setState(5);
				orderServiceImpl.updateEntity(platformOrder);
				response.getWriter().print("success");
			}
		} else {
			response.getWriter().print("fail");
		}
	}
	
	/**
	 * 汇聚代付异步通知
	 * @param request
	 * @param response
	 */
	@PostMapping("/funds/huiju/notify")
	public void huijuCallBack(HttpServletRequest request,HttpServletResponse response){
		String param = CommonUtil.getCommonUtil().getInputStreamParam(request);
		logger.info(String.format("汇聚代付异步通知:%s", param));
		if (!StringUtils.isEmpty(param)){
			JSONObject resp = JSONObject.parseObject(param);
			if (resp.getString("status").equals("205")){
				String merchantOrderNo = resp.getString("merchantOrderNo");
				settleMentServiceImpl.updateStateAndDescBySerialNumber(merchantOrderNo, "汇聚代付成功",new Date());
			}else{
				logger.info(String.format("汇聚代付异步结果:%s", resp.getString("errorCodeDesc")));
			}		
		}
	}
	
	/**
	 * 航天代付异步通知
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	@RequestMapping("/funds/hangtian/notify")
	public void hangtianCallBack(HttpServletRequest request,HttpServletResponse response) throws Exception{
		PPSecurity ppSecurity = MySecurity.getPPSecurity();
		String transData = request.getParameter("transdata");
        logger.info("transdata:{}",transData);
        if(transData != null && transData.startsWith("<?xml")) {
			//说明返回的数据未加密(商户号上送不对时会出现此情况)
		}else if(transData != null){
			transData = new String(ppSecurity.PPFCommDataDecode(transData.getBytes("gbk")), "gbk");
		}
        logger.info("解密后的数据:{}",transData);
        Map<String,String> resp = XmlUtil.toMap(transData.getBytes("gbk"), "gbk");
        if (!StringUtils.isEmpty(resp)){
        	//商户订单号
    		String merOrderNo = resp.get("merorderno").toString();
    		//订单状态 0等待支付 1支付成功 2支付失败
    		String orderStatus = resp.get("orderstatus").toString();
    		if (orderStatus.equals("1")){
    			settleMentServiceImpl.updateStateAndDescBySerialNumber(merOrderNo, "航天代付成功",new Date());
    		}else{
    			logger.info(String.format("航天代付异步结果:%s", resp.get("rspDesc")));
    		}
        }
        response.setContentLength("success".getBytes("utf-8").length);
		response.getOutputStream().write("success".getBytes("utf-8"));
		response.getOutputStream().close();
	}
	
}
