package com.iwanol.paypal.unified;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.vo.Message;

/**
 * 支付下单
 * 
 * @author iwanol
 *
 */
@Component
public class Alipay {
	@Autowired
	private ProdcutServiceImpl prodcutServiceImpl;
	@Autowired
	private Unified unified;
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 支付宝下单
	 * 
	 * @param map
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public void order(Map<String, String> map, HttpServletRequest request, HttpServletResponse response,
			Merchant merchant, Gallery gallery) throws Exception {
		String service = map.get("service");
		String mark = gallery.getGalleryMark().contains("alipay") ? "alipay" : gallery.getGalleryMark();
		String productMark = CoreUtil.getCoreUtil().serviceToProductMark(service, "1");
		Product product = prodcutServiceImpl.findByProductMark(productMark);
		BigDecimal amount = BigDecimal.valueOf(Double.valueOf(map.get("total_fee")) * 0.01);// 下单提交金额
		String sysOrderNumber = CommonUtil.getCommonUtil().createOrder("DS", 4,
				CommonUtil.getCommonUtil().getServerFlag());// 系统订单号
		// 执行数据库操作的公共参数
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
		case "alipay":
			alipay(map, request, response, platformOrder, gallery);
			break; // 支付宝官方
		case "51upay":
			unified.order_51upay(map, platformOrder, request, response);
			break; // 多宝通
		case "ue":
			break; // 优易数卡
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
			unified.order_shuangqian(map, platformOrder, request, response); //双乾支付
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
		case "hangtian":
			unified.order_hangtian(map, platformOrder, request, response);
			break;
		case "jiabei" :
			unified.order_jiabei(map, platformOrder, request, response);
			break;
		}
	}

	public void alipay(Map<String, String> map, HttpServletRequest request, HttpServletResponse response,
			PlatformOrder platformOrder, Gallery gallery) throws ServletException, IOException {
		String page_type = map.get("page_type");
		String subject = "GAMEPAY";
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		String servicePhone = systemSet.getServicePhone();
		String serviceQQ = systemSet.getServiceQQ();
		if (!StringUtils.isEmpty(servicePhone) && !StringUtils.isEmpty(serviceQQ)) {
			subject = "TEL:" + servicePhone + "QQ:" + serviceQQ;
		} else if (!StringUtils.isEmpty(servicePhone)) {
			subject = "TEL:" + servicePhone;
		} else if (!StringUtils.isEmpty(serviceQQ)) {
			subject = "QQ:" + serviceQQ;
		}
		// 封装支付宝下单公共参数
		String uri = map.get("uri");
		Map<String, Object> sParaTemp = new HashMap<String, Object>();
		sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", gallery.getGalleryAccount());
		sParaTemp.put("seller_id", gallery.getGalleryAccount());
		sParaTemp.put("_input_charset", "UTF-8");
		sParaTemp.put("payment_type", "1");
		sParaTemp.put("notify_url", uri + "/zfb_notify");
		sParaTemp.put("out_trade_no", platformOrder.getSysOrderNumber());
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", String.format("%.2f", platformOrder.getAmount().add(platformOrder.getTailAmount().setScale(2)).doubleValue()));
		sParaTemp.put("it_b_pay","3m");
		sParaTemp.put("qr_pay_mode", "4");
		sParaTemp.put("qrcode_width", "240");
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(sParaTemp, null, true, false, true);
		String sign = MD5Util.getMD5Util().sign(signStr, gallery.getGalleryMD5Key(), map.get("charset"));
		sParaTemp.put("sign", sign);
		sParaTemp.put("sign_type", "MD5");
		// 判断是否启用风控
//		if (gallery.getRiskState()) {// 风控 -- 通过代理服务器 重新下单
//			if (!StringUtils.isEmpty(gallery.getRiskDomain())) {
//				System.out.println(gallery.getRiskDomain() + "--" + uri);
//				if (!gallery.getRiskDomain().equals(uri)) {
//					String riskUri = gallery.getRiskDomain();
//					Map<String, String> req = CoreUtil.getCoreUtil().jsonToMap(map.get("reqParam"));
//					//req.put("risk", "true");
//					String json = JSONObject.toJSONString(req);
//					String data = Base64Util.encode(json.getBytes());
//					response.sendRedirect(riskUri + "/gateway?data=" + data);
//				} else {
//					action(platformOrder, gallery, page_type, sParaTemp, response, request);
//				}
//			} else {
//				request.setAttribute("message", Message.result.ret("10038"));
//				request.getRequestDispatcher("/except").forward(request, response);
//			}
//		} else {
//			action(platformOrder, gallery, page_type, sParaTemp, response, request);
//		}
		action(platformOrder, gallery, page_type, sParaTemp, response, request);
	}

	public void action(PlatformOrder platformOrder, Gallery gallery, String page_type, Map<String, Object> sParaTemp,
			HttpServletResponse response, HttpServletRequest request) throws ServletException, IOException {
		try {
			orderServiceImpl.saveEntity(platformOrder);// 持久化订单到数据库
			String qrCodeUrl = gallery.getReqUrl() + "?"
					+ CoreUtil.getCoreUtil().formatUrlMap(sParaTemp, null, false, false, true);
			if (page_type.equals("1")) {
				String returnStr = "{'iframe':'1','status':'0','code_url':'" + qrCodeUrl + "'}";
				response.getWriter().print(returnStr);
			} else {
				Map<String, Object> codeMap = new HashMap<String, Object>();
				codeMap.put("type", "alipay_gf");
				codeMap.put("qrCodeUrl", qrCodeUrl);
				codeMap.put("total_fee", platformOrder.getAmount().add(platformOrder.getTailAmount()).setScale(2,BigDecimal.ROUND_HALF_UP));
				codeMap.put("orderNumber", platformOrder.getSysOrderNumber());
				if (gallery.getRiskState() && !StringUtils.isEmpty(gallery.getRiskDomain())) {
					codeMap.put("riskUri", gallery.getRiskDomain());
				}
				response.setHeader("content-type", "text/html;charset=UTF-8");
				response.getWriter().print(CoreUtil.getCoreUtil().getInputForm(codeMap));
			}
		} catch (Exception e) {
			logger.info("官方支付宝通道下单异常:{}",e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
}
