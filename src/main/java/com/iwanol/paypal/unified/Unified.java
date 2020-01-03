package com.iwanol.paypal.unified;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.domain.SystemSet;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.third.hangtian.ContextUtil;
import com.iwanol.paypal.third.hangtian.HttpUtil;
import com.iwanol.paypal.third.hangtian.MySecurity;
import com.iwanol.paypal.third.hangtian.PPSecurity;
import com.iwanol.paypal.util.BankCode;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.ConnectionUrl;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.Encryption;
import com.iwanol.paypal.util.HmacUtil;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.util.RSAUtil;
import com.iwanol.paypal.util.XmlUtil;
import com.iwanol.paypal.vo.Message;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;

/**
 * 统一下单
 * 
 * @author iwanol
 *
 */
@Component
public class Unified {
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 多宝通统一下单
	 * 
	 * @param map
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void order_51upay(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		try {
			String dbtno = BankCode.getBankCode().dbt(platformOrder.getProduct().getProductMark());
			if (!dbtno.equals("0")) {
				// 多宝通下单参数
				reqData.put("customerid", platformOrder.getGallery().getGalleryAccount());
				reqData.put("sdcustomno", platformOrder.getSysOrderNumber());
				reqData.put("orderAmount", String.format("%.0f", platformOrder.getAmount().doubleValue() * 100));
				reqData.put("cardno", dbtno);
				reqData.put("noticeurl", uri + "/51upay_notify");
				reqData.put("backurl", uri + "/51upay_return");
				reqData.put("remarks", "iwanol:" + platformOrder.getSysOrderNumber());
				reqData.put("mark", platformOrder.getProduct().getProductMark());
				reqData.put("loIp", CommonUtil.getCommonUtil().getIpAddr(request));
				reqData.put("ZFtype", "3");
				String signStr = CoreUtil.getCoreUtil().formatUrlMapToTy("51PayWX", "req", reqData);
				String sign = MD5Util.getMD5Util()
						.sign(signStr, platformOrder.getGallery().getGalleryMD5Key(), map.get("charset")).toUpperCase();
				reqData.put("sign", sign);
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			} else {
				request.setAttribute("message", Message.result.ret("10027"));// 该通道暂不支持此产品
				request.getRequestDispatcher("/except").forward(request, response);
			}
			// 支付宝、微信、QQ钱包
			if (!service.equals("alipay.wap.native") && !service.equals("wechat.wap.native")) {
				String json = ConnectionUrl.getConnectionUrl()
						.getDataFromURL(platformOrder.getGallery().getReqUrl() + "?", reqData, map.get("charset"));
				if (json.contains("null")) {
					json = json.replace("null", "");
				}
				if (json.contains("<head>")) { // 直接跳转页面
					json += "<script type='text/javascript'>document.getElementsByTagName('a')[0].click();</script>";
					response.getWriter().print(json);
				} else {
					Map<String, String> jsonMap = CoreUtil.getCoreUtil().jsonToMap(json);
					if ("1".equals(jsonMap.get("state"))) {
						String url = jsonMap.get("url");
						String returnStr = "";
						if (url.contains("https://openapi.alipay.com/gateway.do")) {// 官方支付宝
							returnStr = "{'status':'0','code_url':'" + url + "'}";
							response.getWriter().print(returnStr);
						} else {
							url = URLDecoder.decode(CoreUtil.getCoreUtil().urlToErCode("dbt", url), "UTF-8");
							if (!url.equals("-1")) {
								if ("1".equals(page_type)) { // 返回二维码地址
									returnStr = "{'status':'0','code_url':'" + uri + "/ercode" + "?url="
											+ Encryption.getEncryption().aesEncrypt(url) + "'}";
									response.getWriter().print(returnStr);
								} else {// 返回页面
									Map<String, Object> codeMap = new HashMap<String, Object>();
									if ("iwanol.alipay.native".equals(service)) {
										codeMap.put("type", "alipay");
									} else if ("iwanol.hbpay.native".equals(service)) {
										codeMap.put("type", "hbpay");
									} else if ("iwanol.wechat.native".equals(service)) {
										codeMap.put("type", "wechat");
									} else {
										codeMap.put("type", "qpay");
									}
									codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(url));
									codeMap.put("total_fee", platformOrder.getAmount());
									codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
									response.setHeader("content-type", "text/html;charset=UTF-8");
									response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
								}
							} else {
								request.setAttribute("message", Message.result.ret("10032"));
								request.getRequestDispatcher("/except").forward(request, response);
							}
						}
					} else {
						request.setAttribute("message", Message.result.ret("10012"));
						request.getRequestDispatcher("/except").forward(request, response);
					}
				}
			} else {// WAP下单
				response.sendRedirect(platformOrder.getGallery().getReqUrl() + "?"
						+ CoreUtil.getCoreUtil().formatUrlMap(reqData, null, false, false, false));
			}
		} catch (Exception e) {
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}

	/**
	 * 威富通
	 * 
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void swift(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		CloseableHttpResponse resp = null;
		CloseableHttpClient client = null;
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String swiftMark = "";
		String body = "";
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(serviceQQ))
			body = "QQ:" + serviceQQ;
		try {
			if (service.equals("iwanol.alipay.native"))
				swiftMark = "pay.alipay.native";
			if (service.equals("iwanol.wechat.native"))
				swiftMark = "pay.weixin.native";
			// 威富通下单参数
			reqData.put("service", swiftMark);
			reqData.put("mch_id", platformOrder.getGallery().getGalleryAccount());
			reqData.put("sign_type", "MD5");
			reqData.put("out_trade_no", platformOrder.getSysOrderNumber());
			reqData.put("body", body + "Order:" + platformOrder.getSysOrderNumber());
			reqData.put("total_fee", String.format("%.0f", platformOrder.getAmount().doubleValue() * 100));
			reqData.put("mch_create_ip", CommonUtil.getCommonUtil().getIpAddr(request));
			reqData.put("notify_url", uri + "/swift_notify");
			reqData.put("nonce_str", String.valueOf(new Date().getTime()));
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(reqData, null, true, false, true);
			String sign = MD5Util.getMD5Util()
					.sign(signStr, "&key=" + platformOrder.getGallery().getGalleryMD5Key(), map.get("charset"))
					.toUpperCase();
			reqData.put("sign", sign.toUpperCase());
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			if (service.equals("iwanol.alipay.native") || service.equals("iwanol.wechat.native")) {
				String xml = XmlUtil.toXml(reqData);
				HttpPost httpPost = new HttpPost(platformOrder.getGallery().getReqUrl());
				StringEntity entityParams = new StringEntity(xml, "utf-8");
				httpPost.setEntity(entityParams);
				httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
				client = HttpClients.createDefault();
				resp = client.execute(httpPost);
				Map<String, String> result = XmlUtil.toMap(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
				if (result.get("status").equals("0")) {
					if ("1".equals(page_type)) {
						String returnStr = "{'status':'0','code_url':'" + result.get("code_url") + "'}";
						response.getWriter().print(returnStr);
					} else {
						Map<String, Object> codeMap = new HashMap<String, Object>();
						if ("iwanol.alipay.native".equals(service)) {
							codeMap.put("type", "alipay");
						}
						if ("iwanol.wechat.native".equals(service)) {
							codeMap.put("type", "wechat");
						}
						codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(result.get("code_url")));
						codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()));
						codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
						response.setHeader("content-type", "text/html;charset=UTF-8");
						response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
					}
				} else {
					request.setAttribute("message", Message.result.ret("10012"));
					request.getRequestDispatcher("/except").forward(request, response);
				}
			} else {
				request.setAttribute("message", Message.result.ret("10012"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		} catch (Exception e) {
			// TODO: handle exception
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}

	}

	/**
	 * 优易数卡
	 * 
	 * @param map
	 *            下单参数
	 * @param platformOrder
	 *            订单信息
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws Exception
	 *             异常
	 */
	public void order_ue(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		// String page_type = map.get("page_type");
		try {
			// 优易下单请求参数
			String frpId = "";
			switch (service) {
			case "iwanol.alipay.native":
				frpId = "alipay";
				break;
			}
			reqData.put("p0_Cmd", "Buy");
			reqData.put("p1_MerId", platformOrder.getGallery().getGalleryAccount());
			reqData.put("p2_Order", platformOrder.getSysOrderNumber());
			reqData.put("p3_Amt", platformOrder.getAmount());
			reqData.put("p4_Cur", "CNY");
			reqData.put("p8_Url", uri + "/ue_notify");
			reqData.put("pd_FrpId", frpId);
			reqData.put("pa_MP", platformOrder.getProduct().getProductMark());
			reqData.put("pr_NeedResponse", "1");
			String hmacStr = CoreUtil.getCoreUtil().formatUrlMapToTy("186ue", "req1", reqData);
			reqData.put("hmac",
					HmacUtil.getHmacUtil().hmacSign(hmacStr, platformOrder.getGallery().getGalleryMD5Key()));
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			if (service.equals("iwanol.alipay.native")) {
				// String result =
				// ConnectionUrl.getConnectionUrl().getDataFromURL(platformOrder.getGallery().getReqUrl()+"?",
				// reqData, map.get("charset"));
			} else {
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		} catch (Exception e) {
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}

	/**
	 * 双乾支付
	 * 
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void order_shuangqian(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> reqData = new HashMap<String, Object>();
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String paymentType = "";
		String body = "";
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(serviceQQ))
			body = "QQ:" + serviceQQ;
		try {
			switch (service) {
				case "iwanol.alipay.native":
					paymentType = "ZFBZF";
					break;	
				case "iwanol.wechat.native":
					paymentType = "WXZF";
					break;
				case "iwanol.qpay.native":
					paymentType = "QQZF";
					break;
				case "iwanol.ecode.native":
					paymentType = "UNION";
					break;
			}
			if (!StringUtils.isEmpty(paymentType)){
				reqData.put("MerNo", platformOrder.getGallery().getGalleryAccount());
				reqData.put("isSubMerPay", "0");
				if (service.equals("iwanol.wechat.native")){
					reqData.put("TranCode", "SMZF010");
				}else{
					reqData.put("TranCode", "SMZF002");
				}
				reqData.put("BillNo", platformOrder.getSysOrderNumber());
				reqData.put("Amount", String.format("%.2f",(platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue()));
				reqData.put("PayType", "SMZF");
				reqData.put("PaymentType", paymentType);
				reqData.put("NotifyURL", uri + "/shuangqian_notify");
				reqData.put("subject", body);
				String sign = MD5Util.getMD5Util().sign(CoreUtil.getCoreUtil().formatUrlMapToTy("shuangqian", "req", reqData), "&"+MD5Util.getMD5Util().MD5(platformOrder.getGallery().getGalleryMD5Key()).toUpperCase(), "utf-8").toUpperCase();
				reqData.put("MD5info", sign);
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
				logger.info("【请求参数:{}】",JSONObject.toJSONString(reqData));
				String result = ConnectionUrl.getConnectionUrl().getDataFromURL(platformOrder.getGallery().getReqUrl(), reqData, "UTF-8");
				logger.info(result);
				JSONObject resp = null;
				if (service.equals("iwanol.wechat.native")){
					 resp = JSONObject.parseObject(result);
				}else{
					 resp = JSONObject.parseArray(result).getJSONObject(0);
				}
				if (page_type.equals("1")){				
					if (resp.getString("respCode").equals("000000")){
						String returnStr = "";
						if (service.equals("iwanol.wechat.native")){
							returnStr = "{'status':'0','code_url':'" + resp.getString("qrUrl") + "'}";
						}else{
							returnStr = "{'status':'0','code_url':'" + resp.getString("qrCode") + "'}";
						}
						response.getWriter().print(returnStr);
					}else{
						String returnStr = "{'status':'-1','msg':'" + resp.getString("respMess") + "'}";
						response.getWriter().print(returnStr);
					}
				}else{
					if (resp.getString("respCode").equals("000000")){
						
						Map<String, Object> codeMap = new HashMap<String, Object>();
						if ("iwanol.alipay.native".equals(service)) {
							codeMap.put("type", "alipay");
							codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("qrCode")));
						}
						if ("iwanol.wechat.native".equals(service)) {
							codeMap.put("type", "wechat");
							codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("qrUrl")));
						}
						if ("iwanol.qpay.native".equals(service)) {
							codeMap.put("type", "qpay");
							codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("qrCode")));
						}
						if ("iwanol.ecode.native".equals(service)) {
							codeMap.put("type", "ecode");
							codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("qrCode")));
						}				
						codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()));
						codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
						response.setHeader("content-type", "text/html;charset=UTF-8");
						response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
						
					}else{
						request.setAttribute("message", Message.result.msg("10012", resp.getString("respMess")));
						request.getRequestDispatcher("/except").forward(request, response);
					}
				}
			}else{
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 汇聚支付
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_huiju(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> reqData = new HashMap<String, Object>();
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String q1_FrpCode = "";
		String body = "";
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(serviceQQ))
			body = "QQ:" + serviceQQ;
		if (service.equals("iwanol.alipay.native"))
			q1_FrpCode = "ALIPAY_NATIVE";
		if (service.equals("iwanol.wechat.native"))
			q1_FrpCode = "WEIXIN_NATIVE";
		if (service.equals("iwanol.qpay.native"))
			q1_FrpCode = "QQ_NATIVE";
		if (service.equals("iwanol.ecode.native"))
			q1_FrpCode = "UNIONPAY_NATIVE";
		try{
			if (!StringUtils.isEmpty(q1_FrpCode)){
				reqData.put("p0_Version", "1.0");
				reqData.put("p1_MerchantNo",platformOrder.getGallery().getGalleryAccount());
				reqData.put("p2_OrderNo",platformOrder.getSysOrderNumber());
				reqData.put("p3_Amount",String.format("%.2f",(platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue()));
				reqData.put("p4_Cur","1");
				reqData.put("p5_ProductName",body);
				reqData.put("p9_NotifyUrl", "AB|"+uri + "/huiju_notify");
				reqData.put("q1_FrpCode", q1_FrpCode);
				String content = "1.0".concat(platformOrder.getGallery().getGalleryAccount()).concat(platformOrder.getSysOrderNumber())
						.concat(String.format("%.2f",(platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue())).concat("1").concat(body).concat("AB|"+ uri + "/huiju_notify")
						.concat(q1_FrpCode).concat(platformOrder.getGallery().getGalleryMD5Key());
				reqData.put("hmac",DigestUtils.md5DigestAsHex(content.getBytes()).toUpperCase());
				
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
				String result = ConnectionUrl.getConnectionUrl().getDataFromURL(platformOrder.getGallery().getReqUrl(), reqData, "UTF-8");
				logger.info("汇聚下单响应:{}",result);
				JSONObject resp = JSONObject.parseObject(result);
				if (page_type.equals("1")){				
					if (resp.getString("ra_Code").equals("100")){
						String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(resp.getString("rc_Result"),"utf-8") + "'}";
						response.getWriter().print(returnStr);
					}else{
						String returnStr = "{'status':'-1','msg':'" + resp.getString("respMess") + "'}";
						response.getWriter().print(returnStr);
					}
				}else{
					if (resp.getString("ra_Code").equals("100")){						
						Map<String, Object> codeMap = new HashMap<String, Object>();
						if ("iwanol.alipay.native".equals(service)) {
							codeMap.put("type", "alipay");
						}
						if ("iwanol.wechat.native".equals(service)) {
							codeMap.put("type", "wechat");
						}
						if ("iwanol.qpay.native".equals(service)){
							codeMap.put("type", "qpay");
						}
						if ("iwanol.ecode.native".equals(service)){
							codeMap.put("type", "ecode");
						}
						codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("rc_Result")));	
						codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
						codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
						response.setHeader("content-type", "text/html;charset=UTF-8");
						response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
						
					}else{
						request.setAttribute("message", Message.result.msg("10012", resp.getString("respMess")));
						request.getRequestDispatcher("/except").forward(request, response);
					}
				}
			}else{
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("汇聚下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 掌灵科技
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_zhangling(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> reqData = new HashMap<String, Object>();
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String payChannelId = "";
		String body = "";
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(serviceQQ))
			body = "QQ:" + serviceQQ;
		if (service.equals("iwanol.alipay.native") || service.equals("iwanol.hbpay.native"))
			payChannelId = "0000000002";
		if (service.equals("iwanol.wechat.native"))
			payChannelId = "2100000001";
		if (service.equals("iwanol.qpay.native"))
			payChannelId = "2000000003";		
		try{
			if (!StringUtils.isEmpty(payChannelId)){
				reqData.put("amount", String.format("%.0f",(platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue() * 100));
				reqData.put("appid",platformOrder.getGallery().getGalleryAccount());
				reqData.put("subject",body);
				reqData.put("mchntOrderNo",platformOrder.getSysOrderNumber());
				reqData.put("payChannelId",payChannelId);
				reqData.put("version","api_NoEncrypt");
				reqData.put("clientIp", CommonUtil.getCommonUtil().getIpAddr(request));
				reqData.put("notifyUrl", uri + "/zhangling_notify");
				String signStr = CoreUtil.getCoreUtil().formatUrlMap(reqData, null, true, false, true);
				reqData.put("signature", MD5Util.getMD5Util().sign(signStr, "&key="+platformOrder.getGallery().getGalleryMD5Key(), "utf-8"));
				String param = Base64.encodeBase64String(RSAUtil.encryptByPublicKeyByPKCS1Padding(JSONObject.toJSONString(reqData).getBytes("utf-8"), platformOrder.getGallery().getGalleryPubKey()));
				logger.info("掌灵下单报文:{}",param);
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
				String result = HttpRequest.post(platformOrder.getGallery().getReqUrl()).body("orderInfo="+param.replace("+", "%2B")).execute().body();
				logger.info("掌灵下单响应参数:{}",result);
				JSONObject resp = JSONObject.parseObject(result);
				if (page_type.equals("1")){
					if (resp.getString("respCode").equals("200")){
						Map<String,Object> respMap = CommonUtil.getCommonUtil().getUrlParams(resp.getString("extra"));
						String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(respMap.get("code_url").toString(),"utf-8") + "'}";
						response.getWriter().print(returnStr);
					}else{
						String returnStr = "{'status':'-1','msg':'" + resp.getString("respMsg") + "'}";
						response.getWriter().print(returnStr);
					}
				}else{
					if (resp.getString("respCode").equals("200")){						
						Map<String, Object> codeMap = new HashMap<String, Object>();
						Map<String,Object> respMap = CommonUtil.getCommonUtil().getUrlParams(resp.getString("extra"));
						if ("iwanol.alipay.native".equals(service)) {
							codeMap.put("type", "alipay");
						}
						if ("iwanol.wechat.native".equals(service)) {
							codeMap.put("type", "wechat");
						}
						if ("iwanol.qpay.native".equals(service)){
							codeMap.put("type", "qpay");
						}
						if ("iwanol.hbpay.native".equals(service)){
							codeMap.put("type", "hbpay");
						}
						
						codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(respMap.get("code_url").toString()));	
						codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
						codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
						Gallery gallery = platformOrder.getGallery();
						if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
							codeMap.put("riskUri", gallery.getRiskDomain());
						}
						response.setHeader("content-type", "text/html;charset=UTF-8");
						response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
						
					}else{
						request.setAttribute("message", Message.result.msg("10012", resp.getString("respMsg")));
						request.getRequestDispatcher("/except").forward(request, response);
					}
				}
			}else{
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("掌灵下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 万像
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_wanxiang(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String type = "";
		if (service.equals("iwanol.alipay.native") || service.equals("iwanol.hbpay.native"))
			type = "ALIPAY";
		if (service.equals("iwanol.wechat.native"))
			type = "WEIXIN";	
		try{
			reqData.put("version", "2.1");
			reqData.put("parter", platformOrder.getGallery().getGalleryAccount());
			reqData.put("type", type);
			reqData.put("amount",String.format("%.2f",(platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue()));
			reqData.put("orderno",platformOrder.getSysOrderNumber());
			reqData.put("recefiveurl", uri + "/huiju_ebank_ret");
			reqData.put("notifyurl", uri + "/wanxiang_notify");
			reqData.put("remark", "iwanol");
			reqData.put("orderencodetype", "MD5");
			reqData.put("qrcode", "1");
			StringBuffer buffer = new StringBuffer();
			buffer.append("version=2.1").append("&parter="+reqData.get("parter")).append("&type="+reqData.get("type"))
			.append("&amount="+reqData.get("amount")).append("&orderno="+reqData.get("orderno"))
			.append("&recefiveurl="+reqData.get("recefiveurl")).append("&notifyurl="+reqData.get("notifyurl"))
			.append("&remark="+reqData.get("remark")).append("&orderencodetype=MD5");
			reqData.put("sign", MD5Util.getMD5Util().sign(buffer.toString(), "&key="+platformOrder.getGallery().getGalleryMD5Key(), "utf-8"));
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			String result = HttpRequest.post(platformOrder.getGallery().getReqUrl()).body(CoreUtil.getCoreUtil().formatUrlMap(reqData, null, false, false, true)).execute().body();
			JSONObject resp = JSONObject.parseObject(result);
			if (page_type.equals("1")){
				if (resp.getString("State").equals("Success")){
					String returnStr = "{'status':'0','qrcodeImage':'1','code_url':'" + URLEncoder.encode(resp.get("Data").toString(),"utf-8") + "'}";
					response.getWriter().print(returnStr);
				}else{
					String returnStr = "{'status':'-1','msg':'" + resp.getString("Msg") + "'}";
					response.getWriter().print(returnStr);
				}
			}else{
				if (resp.getString("State").equals("Success")){						
					Map<String, Object> codeMap = new HashMap<String, Object>();
					if ("iwanol.alipay.native".equals(service)) {
						codeMap.put("type", "alipay");
					}
					if ("iwanol.wechat.native".equals(service)) {
						codeMap.put("type", "wechat");
					}
					
					if ("iwanol.hbpay.native".equals(service)){
						codeMap.put("type", "hbpay");
					}
					
					codeMap.put("codeImage", "1");
					codeMap.put("qrCodeUrl", resp.get("Data").toString());	
					codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
					codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
					Gallery gallery = platformOrder.getGallery();
					if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
						codeMap.put("riskUri", gallery.getRiskDomain());
					}
					response.setHeader("content-type", "text/html;charset=UTF-8");
					response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
					
				}else{
					request.setAttribute("message", Message.result.msg("10012", resp.getString("respMsg")));
					request.getRequestDispatcher("/except").forward(request, response);
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("万像下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	public void order_jiabei(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String,Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String type = "";
		String bankCode = "";
		Gallery gallery = platformOrder.getGallery();
		if (service.equals("iwanol.alipay.native"))
			type = "alipay";
		if (service.equals("iwanol.wechat.native"))
			type = "wxcode";
		if (service.equals("iwanol.qpay.native"))
			type = "qqpay";
		if (service.equals("iwanol.bank.native")){
			type = "onlinebank";
			bankCode = BankCode.getBankCode().jiabei(map.get("bank_code"));
		}	
		if (service.equals("iwanol.ecode.native"))
			type = "unionpay";
		if (service.equals("iwanol.hbpay.native"))
			type = "hbpay";
		try{
			reqData.put("ApiMethod", "OnLinePay");
			reqData.put("Version", "V2.0");
			reqData.put("MerID", gallery.getGalleryAccount());
			reqData.put("TradeNum", platformOrder.getSysOrderNumber());
			reqData.put("Amount",String.format("%.2f", platformOrder.getAmount().doubleValue()));
			reqData.put("NotifyUrl", uri + "/jiabei_notify");
			reqData.put("TransTime",CommonUtil.getCommonUtil().currentDateTime("yyyyMMddHHmmss", new Date()));
			reqData.put("PayType", type);
			if (type.equals("onlinebank"))
				reqData.put("BankCode", bankCode);
			reqData.put("SignType","MD5");
			reqData.put("IsImgCode","1");
			reqData.put("UserIP",CommonUtil.getCommonUtil().getIpAddr(request)); //CommonUtil.getCommonUtil().getIpAddr(request)
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(reqData, null, true, false, true);
			String sign = MD5Util.getMD5Util().sign(signStr, "&key=".concat(gallery.getGalleryMD5Key()), "UTF-8");
			reqData.put("Sign", sign);
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			String result = HttpRequest.post(gallery.getReqUrl()).body(CoreUtil.getCoreUtil().formatUrlMap(reqData, null, false, false, true)).execute().body();
			logger.info("嘉贝下单响应:{}",result);
			JSONObject resp = JSONObject.parseObject(result);
			if (page_type.equals("1")){
				if (resp.getString("RespCode").equals("1111")){
					String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(resp.getString("PayUrl"),"utf-8") + "'}";
					response.getWriter().print(returnStr);
				}else{
					String returnStr = "{'status':'-1','msg':'" + resp.getString("Message") + "'}";
					response.getWriter().print(returnStr);
				}
			}else{
				if (resp.getString("RespCode").equals("1111")){
					Map<String, Object> codeMap = new HashMap<String, Object>();
					if ("iwanol.alipay.native".equals(service)) {
						codeMap.put("type", "alipay");
					}
					if ("iwanol.wechat.native".equals(service)) {
						codeMap.put("type", "wechat");
					}
					if ("iwanol.qpay.native".equals(service)) {
						codeMap.put("type", "qpay");
					}
					if ("iwanol.bank.native".equals(service)) {
						codeMap.put("type", "ebank");
					}
					if ("iwanol.ecode.native".equals(service)) {
						codeMap.put("type", "ecode");
					}
					if ("iwanol.hbpay.native".equals(service)) {
						codeMap.put("type", "hbpay");
					}
					codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("PayUrl")));	
					codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
					codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
					if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
						codeMap.put("riskUri", gallery.getRiskDomain());
					}
					response.setHeader("content-type", "text/html;charset=UTF-8");
					response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
				}else{
					request.setAttribute("message", Message.result.msg("10012", resp.getString("Message")));
					request.getRequestDispatcher("/except").forward(request, response);
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("嘉贝下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 龙宝
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_longbao(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String service = map.get("service");
		String page_type = map.get("page_type");
		String type = "";
		if (service.equals("iwanol.alipay.native"))
			type = "1003";
		if (service.equals("iwanol.wechat.native"))
			type = "1004";
		if (service.equals("iwanol.cft.native"))
			type = "993";
		try{
			reqData.put("parter", platformOrder.getGallery().getGalleryAccount());
			reqData.put("orderid", platformOrder.getSysOrderNumber());
			reqData.put("callbackurl", uri + "/longbao_notify");
			reqData.put("hrefbackurl", uri + "/huiju_ebank_ret");
			reqData.put("attach", "QYIOL");
			reqData.put("type", type);
			reqData.put("value", String.format("%.2f", (platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue()));
			StringBuffer buffer = new StringBuffer();
			buffer.append("parter="+reqData.get("parter")).append("&type="+reqData.get("type")).append("&value="+reqData.get("value"))
			.append("&orderid="+reqData.get("orderid")).append("&callbackurl="+reqData.get("callbackurl"));
			String sign = MD5Util.getMD5Util().sign(buffer.toString(), platformOrder.getGallery().getGalleryMD5Key(), "GB2312");
			reqData.put("sign", sign);
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			String result = HttpRequest.post(platformOrder.getGallery().getReqUrl()).body(CoreUtil.getCoreUtil().formatUrlMap(reqData, null, false, false, true)).execute().body();
			logger.info("龙宝下单响应:{}",result);
			JSONObject resp = JSONObject.parseObject(result);
			if (page_type.equals("1")){
				if (resp.getString("code").equals("0")){
					String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(resp.getString("url"),"utf-8") + "'}";
					response.getWriter().print(returnStr);
				}else{
					String returnStr = "{'status':'-1','msg':'" + resp.getString("msg") + "'}";
					response.getWriter().print(returnStr);
				}
			}else{
				if (resp.getString("code").equals("0")){
					Map<String, Object> codeMap = new HashMap<String, Object>();
					if ("iwanol.alipay.native".equals(service)) {
						codeMap.put("type", "alipay");
					}
					if ("iwanol.wechat.native".equals(service)) {
						codeMap.put("type", "wechat");
					}
					if ("iwanol.cft.native".equals(service)) {
						codeMap.put("type", "cft");
					}
					
					codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resp.getString("url")));	
					codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
					codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
					Gallery gallery = platformOrder.getGallery();
					if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
						codeMap.put("riskUri", gallery.getRiskDomain());
					}
					response.setHeader("content-type", "text/html;charset=UTF-8");
					response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
				}else{
					request.setAttribute("message", Message.result.msg("10012", resp.getString("msg")));
					request.getRequestDispatcher("/except").forward(request, response);
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("龙宝下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 银联商务接口
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_union(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try{
			JSONObject sParaTemp = new JSONObject();
			String service = map.get("service");
			String page_type = map.get("page_type");
			String uri = map.get("uri");
			Gallery gallery= platformOrder.getGallery();
			String mid = gallery.getGalleryAccount().split("\\|")[0];
			String tid = gallery.getGalleryAccount().split("\\|")[1];
			String instMid = gallery.getGalleryAccount().split("\\|")[2];
			String msgSrc = gallery.getGalleryAccount().split("\\|")[3];
			String msgSrcId = gallery.getGalleryAccount().split("\\|")[4];
			String billNo = msgSrcId.concat(CommonUtil.getCommonUtil().currentDateTime("yyyyMMHHmmssSSS", new Date())).concat(CommonUtil.getCommonUtil().random(7));
			platformOrder.setSysOrderNumber(billNo);
			sParaTemp.put("msgSrc", msgSrc);
			sParaTemp.put("msgType","bills.getQRCode");
			sParaTemp.put("requestTimestamp",CommonUtil.getCommonUtil().currentDateTime("yyyy-MM-dd HH:mm:ss", new Date()));
			sParaTemp.put("mid", mid);
			sParaTemp.put("tid", tid);
			sParaTemp.put("instMid", instMid);
			sParaTemp.put("billNo", billNo);
			//sParaTemp.put("qrCodeId", msgSrcId.concat(CommonUtil.getCommonUtil().currentDateTime("yyyyMMHHmmssSSS", new Date())).concat(CommonUtil.getCommonUtil().random(7)));
			sParaTemp.put("billDate", CommonUtil.getCommonUtil().currentDateTime("yyyy-MM-dd",new Date()));
			sParaTemp.put("totalAmount", String.format("%.2f", (platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue()));
			sParaTemp.put("notifyUrl", uri+"/union_notify");
			String signStr = CoreUtil.getCoreUtil().formatUrlMap(sParaTemp, null, true, false, true);
			String sign = MD5Util.getMD5Util().sign(signStr, gallery.getGalleryMD5Key(),"UTF-8");
			sParaTemp.put("sign", sign.toUpperCase());
			logger.info("下单请求参数:{}",JSONObject.toJSONString(sParaTemp));
			//String url = "https://qr-test2.chinaums.com/bills/qrCode.do?id="+sParaTemp.getString("qrCodeId");
			//logger.info("二维码url:{}",url);
			String result = HttpRequest.post(gallery.getReqUrl()).body(JSONObject.toJSONString(sParaTemp)).execute().body();
			logger.info("银联商务下单响应:{}",result);
			JSONObject resultObj = JSONObject.parseObject(result);
			if (page_type.equals("1")){
				if (resultObj.getString("errCode").equals("SUCCESS")){
					String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(resultObj.getString("billQRCode"),"utf-8") + "'}";
					response.getWriter().print(returnStr);
				}else{
					String returnStr = "{'status':'-1','msg':'" + resultObj.getString("errMsg") + "'}";
					response.getWriter().print(returnStr);
				}
			}else{
				if (resultObj.getString("errCode").equals("SUCCESS")){
					Map<String, Object> codeMap = new HashMap<String, Object>();
					if ("iwanol.alipay.native".equals(service)) {
						codeMap.put("type", "alipay");
					}
					if ("iwanol.wechat.native".equals(service)) {
						codeMap.put("type", "wechat");
					}
					
					if ("iwanol.ecode.native".equals(service)){
						codeMap.put("type", "ecode");
					}
					codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(resultObj.getString("billQRCode")));	
					codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
					codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
					response.setHeader("content-type", "text/html;charset=UTF-8");
					response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
				}else{
					request.setAttribute("message", Message.result.msg("10012", resultObj.getString("errMsg")));
					request.getRequestDispatcher("/except").forward(request, response);
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	/**
	 * 航天微信
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	public void order_hangtian_wx(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String page_type = map.get("page_type");
		String uri = map.get("uri");
		//判断是否启用风控
		Gallery gallery = platformOrder.getGallery();
		hangtianWxAction(platformOrder, gallery, uri,page_type,request,response);
	}
	
	public void hangtianWxAction(PlatformOrder platformOrder,Gallery gallery,String uri,String page_type,HttpServletRequest request,HttpServletResponse response) throws Exception{
		try{
			if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
				uri = gallery.getRiskDomain();
			}
			String callbackUrl = uri + "/callback/hangtian";
			StringBuffer qrCodeUrl = new StringBuffer();
			qrCodeUrl.append("https://open.weixin.qq.com/connect/oauth2/authorize?").append("appid="+gallery.getAppId()).append("&redirect_uri="+callbackUrl)
			.append("&response_type=code").append("&scope=snsapi_base").append("&state="+platformOrder.getSysOrderNumber()).append("#wechat_redirect");
			logger.info("微信callbackUrl:{}",qrCodeUrl.toString());
			orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
			if (page_type.equals("1")){
				String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(qrCodeUrl.toString(),"utf-8") + "'}";
				response.getWriter().print(returnStr);
			}else{
				Map<String, Object> codeMap = new HashMap<String, Object>();
				codeMap.put("type", "wechat");			
				codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(qrCodeUrl.toString()));	
				codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
				codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
				if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
					codeMap.put("riskUri", gallery.getRiskDomain());
				}
				response.setHeader("content-type", "text/html;charset=UTF-8");
				response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
		
	}
	
	/**
	 * 航天支付宝
	 * @param map
	 * @param platformOrder
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void order_hangtian(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try{
			PPSecurity ppSecurity = MySecurity.getPPSecurity();
			Map<String, String> reqData = new HashMap<String, String>();
			String uri = map.get("uri");
			String service = map.get("service");
			String page_type = map.get("page_type");
			String productid = "ail_NATIVE0201";
			String paystyle = "4";
			reqData.put("opcode", "MA5001");
			reqData.put("productid", productid);
			reqData.put("merorderno", platformOrder.getSysOrderNumber());
			reqData.put("merorderdate",CommonUtil.getCommonUtil().currentDateTime("yyyyMMdd", new Date()));
			reqData.put("meruserid", UUID.fastUUID().toString(true));
			reqData.put("tranamt", String.format("%.0f", (platformOrder.getAmount().add(platformOrder.getTailAmount())).doubleValue() * 100));
			reqData.put("orderdesc","QQ:177752992("+platformOrder.getSysOrderNumber()+"");
			reqData.put("notifyurl", uri + "/hangtian_notify");
			reqData.put("paystyle", paystyle);
			hangtianAction(ppSecurity, reqData, platformOrder, page_type, service, response, request);
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	public void hangtianAction(PPSecurity ppSecurity,Map<String, String> reqData,PlatformOrder platformOrder,String page_type,String service,HttpServletResponse response,HttpServletRequest request) throws Exception{
		String xml = XmlUtil.map2Xml(reqData);
		logger.info("下单请求xml:{}",xml);
		byte[] encodeDataByte = ppSecurity.PPFCommDataEncode(xml.getBytes("gbk"));
		xml = new String(encodeDataByte,"gbk");
		xml = URLEncoder.encode(xml,"gbk");
		String merid = ContextUtil.getValue("merid");
		String data = "merid="+merid+"&transdata="+xml;
		logger.info("最终请求数据:{}",data);
		String res = HttpUtil.request(platformOrder.getGallery().getReqUrl(),data,true);
		//加密响应数据
		byte[] resB = ppSecurity.PPFCommDataDecode(res.getBytes("gbk"));
		logger.info("航天下单响应参数:{}",new String(resB,"gbk"));
		Map<String,String> ret = XmlUtil.toMap(resB,"gbk");
		logger.info("xml转Map:",ret);
		orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
		if (page_type.equals("1")){
			if (ret.get("rspcode").equals("000000")){
				String returnStr = "{'status':'0','code_url':'" + URLEncoder.encode(ret.get("payurl"),"utf-8") + "'}";
				response.getWriter().print(returnStr);
			}else{
				String returnStr = "{'status':'-1','msg':'" + ret.get("rspdesc") + "'}";
				response.getWriter().print(returnStr);
			}
		}else{
			if (ret.get("rspcode").equals("000000")){
				Map<String, Object> codeMap = new HashMap<String, Object>();
				if ("iwanol.alipay.native".equals(service)) {
					codeMap.put("type", "alipay");
				}
				if ("iwanol.wechat.native".equals(service)) {
					codeMap.put("type", "wechat");
				}		
				
				if ("iwanol.hbpay.native".equals(service)){
					codeMap.put("type", "hbpay");
				}
				
				codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(ret.get("payurl")));	
				codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
				codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
				Gallery gallery = platformOrder.getGallery();
				if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
					codeMap.put("riskUri", gallery.getRiskDomain());
				}
				response.setHeader("content-type", "text/html;charset=UTF-8");
				response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
			}else{
				request.setAttribute("message", Message.result.msg("10012", ret.get("rspdesc")));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		}
	}
}
