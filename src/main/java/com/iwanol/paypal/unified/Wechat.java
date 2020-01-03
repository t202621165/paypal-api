package com.iwanol.paypal.unified;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.util.StringUtils;

import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.domain.Product;
import com.iwanol.paypal.domain.SystemSet;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.service.impl.ProdcutServiceImpl;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.Encryption;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.util.XmlUtil;
import com.iwanol.paypal.vo.Message;

import cn.hutool.core.lang.UUID;

/**
 * 微信下单
 * 
 * @author iwanol
 *
 */
@Component
public class Wechat {
	@Autowired
	private ProdcutServiceImpl prodcutServiceImpl;
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	@Autowired 
	private Unified unified;
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 微信下单
	 * 
	 * @param map
	 * @param request
	 * @param response
	 * @param merchant
	 * @param gallery
	 * @throws Exception
	 */
	public void order(Map<String, String> map, HttpServletRequest request, HttpServletResponse response,
			Merchant merchant, Gallery gallery) throws Exception {
		String service = map.get("service");
		// String page_type = map.get("page_type");
		String mark = gallery.getGalleryMark();
		String productMark = CoreUtil.getCoreUtil().serviceToProductMark(service, "1");
		Product product = prodcutServiceImpl.findByProductMark(productMark);
		BigDecimal amount = BigDecimal.valueOf(Double.valueOf(map.get("total_fee")) * 0.01);// 下单提交金额
		String sysOrderNumber = CommonUtil.getCommonUtil().createOrder("DS", 4,
				CommonUtil.getCommonUtil().getServerFlag());// 系统订单号
		// 执行数据库操作的公告参数
		PlatformOrder platformOrder = new PlatformOrder();
		if (map.containsKey("agency_id")){
			platformOrder.setAgencyAccount(map.get("agency_id"));
		}
		platformOrder.setOrderDate(new Date());
		platformOrder.setSysOrderNumber(sysOrderNumber);
		platformOrder.setMerchantOrderNumber(map.get("merchant_order"));
		platformOrder.setAmount(amount);
		if (merchant.getIsOpenTail()){
			String scope = String.valueOf(systemSetServiceImpl.findTailScope("iwanol"));
			logger.info("风控金额范围:{}-{}",scope,1);
			platformOrder.setTailAmount(CommonUtil.getCommonUtil().tailAmount(scope));
		}
		platformOrder.setState(0);
		platformOrder.setMerchant(merchant);
		if (map.containsKey("attach")) {
			platformOrder.setAttach(map.get("attach"));
		}
		platformOrder.setClientIp(CommonUtil.getCommonUtil().getIpAddr(request));
		platformOrder.setReqParam(map.get("reqParam"));
		platformOrder.setGallery(gallery);
		platformOrder.setProduct(product);
		platformOrder.setNotifyUrl(map.get("notify_url"));
		platformOrder.setRetUrl(map.get("return_url"));
		switch (mark) {
		case "wechat":
			wechat(map, platformOrder, request, response);
			break; // 微信官方
		case "51upay":
			unified.order_51upay(map, platformOrder, request, response);
			break; // 多宝通微信
		case "swift":
			unified.swift(map, platformOrder, request, response); //威富通
			break;
		case "swift1":
			unified.swift(map, platformOrder, request, response); //威富通
			break;
		case "swift2":
			unified.swift(map, platformOrder, request, response); //威富通
			break;
		case "shuangqian":
			unified.order_shuangqian(map, platformOrder, request, response);
			break;
		case "huiju":
			unified.order_huiju(map, platformOrder, request, response);
			break;
		case "union":
			unified.order_union(map, platformOrder, request, response);
			break;
		case "zhangling":
			unified.order_zhangling(map, platformOrder, request, response);
			break;
		case "zhangling_1":
			unified.order_zhangling(map, platformOrder, request, response);
			break;
		case "wanxiang":
			unified.order_wanxiang(map, platformOrder, request, response);
			break;
		case "longbao":
			unified.order_longbao(map, platformOrder, request, response);
			break;
		case "hangtian_wx":
			unified.order_hangtian_wx(map, platformOrder, request, response);
			break;
		case "jiabei" :
			unified.order_jiabei(map, platformOrder, request, response);
			break;
		}
	}

	public void wechat(Map<String, String> map,PlatformOrder platformOrder, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CloseableHttpResponse resp = null;
		CloseableHttpClient client = null;
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		Map<String, Object> reqData = new HashMap<String, Object>();
		String uri = map.get("uri");
		String page_type = map.get("page_type");
		String body = "";
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(serviceQQ))
			body = "QQ:" + serviceQQ;
		reqData.put("trade_type", "NATIVE");
		reqData.put("appId", platformOrder.getGallery().getAppId());
		reqData.put("mch_id", platformOrder.getGallery().getGalleryAccount());
		reqData.put("notify_url", uri +"/wechat_notify");
		reqData.put("nonce_str", UUID.fastUUID().toString().replace("-", ""));
		reqData.put("out_trade_no", platformOrder.getSysOrderNumber());
		reqData.put("body", body);
		reqData.put("total_fee", String.format("%.2f", platformOrder.getAmount().doubleValue() * 100));
		reqData.put("spbill_create_ip", CommonUtil.getCommonUtil().getIpAddr(request));
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(reqData, null, true, false, true);
		String sign = MD5Util.getMD5Util()
				.sign(signStr, "&key=" + platformOrder.getGallery().getGalleryMD5Key(), map.get("charset"))
				.toUpperCase();
		reqData.put("sign", sign);
		orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
		try{
			String xml = XmlUtil.toXml(reqData);
			HttpPost httpPost = new HttpPost(platformOrder.getGallery().getReqUrl());
			StringEntity entityParams = new StringEntity(xml, "utf-8");
			httpPost.setEntity(entityParams);
			httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
			client = HttpClients.createDefault();
			resp = client.execute(httpPost);
			Map<String, String> result = XmlUtil.toMap(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
			logger.info("官方微信下单响应参数:{}",result);
			if (result.containsKey("code_url")){
				if ("1".equals(page_type)) {
					String returnStr = "{'status':'0','code_url':'" + result.get("code_url") + "'}";
					response.getWriter().print(returnStr);
				} else {
					Map<String, Object> codeMap = new HashMap<String, Object>();
					codeMap.put("type", "wechat");
					codeMap.put("qrCodeUrl", Encryption.getEncryption().aesEncrypt(result.get("code_url")));
					codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()));
					codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
					response.setHeader("content-type", "text/html;charset=UTF-8");
					response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
				}
			}else{
				request.setAttribute("message", Message.result.ret("10012"));
				request.getRequestDispatcher("/except").forward(request, response);
			}	
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("微信官方下单异常:{}",e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
		logger.info("【下单失败^_^:微信官方通道暂未开通】");
	}

}
