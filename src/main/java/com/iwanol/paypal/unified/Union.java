package com.iwanol.paypal.unified;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.domain.Product;
import com.iwanol.paypal.service.impl.ProdcutServiceImpl;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.CoreUtil;
/**
 * 银联
 * @author iwano
 */
@Component
public class Union {
	@Autowired
	private ProdcutServiceImpl prodcutServiceImpl;
	@Autowired
	private Unified unified;
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	// @Autowired
	// private OrderServiceImpl orderServiceImpl;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 
	 * @param map
	 *            下单参数
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param merchant
	 *            商户信息
	 * @param gallery
	 *            通道信息
	 * @throws Exception
	 *             异常
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
		case "union":
			union(map, platformOrder, request, response);
			break; // 银联官方扫码
		case "shuangqian":
			unified.order_shuangqian(map, platformOrder, request, response); //双乾
			break;
		case "huiju":
			unified.order_huiju(map, platformOrder, request, response);
			break;
		case "jiabei" :
			unified.order_jiabei(map, platformOrder, request, response);
			break;
		}
	}

	public void union(Map<String,String> map,PlatformOrder platformOrder,HttpServletRequest request,HttpServletResponse response) {
		logger.info("暂不支持该银联通道");
	}
}
