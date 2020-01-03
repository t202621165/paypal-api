package com.iwanol.paypal.unified;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.domain.Product;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.service.impl.ProdcutServiceImpl;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.vo.Message;

@Component
public class Ebank {
	@Autowired
	private ProdcutServiceImpl prodcutServiceImpl;
	@Autowired
	private OrderServiceImpl orderServiceImpl;
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
		String mark = gallery.getGalleryMark();
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
		platformOrder.setNotifyUrl(map.get("notify_url"));
		platformOrder.setRetUrl(map.get("return_url"));
		switch (mark) {
		case "bank":
			bank();
			break; // 网银官方扫码
		case "shuangqian_ebank":
			order_shuangqian_ebank(map, platformOrder, request, response); //双乾
			break;
		case "huiju_ebank":
			order_huiju_ebank(map, platformOrder, request, response);
			break;
		case "jiabei" :
			unified.order_jiabei(map, platformOrder, request, response);
			break;
		}
	}

	public void bank() {
		logger.info("【下单失败^_^:网银官方扫码通道暂未开通】");
	}
	
	public void order_huiju_ebank(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException{
		String uri = map.get("uri");
		String service = map.get("service");
		try{
			if (service.equals("iwanol.bank.native")){
				String amount = String.format("%.2f",platformOrder.getAmount().add(platformOrder.getTailAmount()).doubleValue());
				String merNo = platformOrder.getGallery().getGalleryAccount();
				Product product = prodcutServiceImpl.findByProductMark(map.get("bank_code"));
				String paymentType = getBankMark(map.get("bank_code"), "huiju");
				platformOrder.setProduct(product);
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
				StringBuffer sbHtml = new StringBuffer();
				String signStr = merNo.concat(platformOrder.getSysOrderNumber()).concat(amount).concat("1")
						.concat("IWANOL").concat(uri+"/huiju_ebank_ret").concat("AB|" +uri+"/huiju_notify")
						.concat(paymentType).concat("0").concat(platformOrder.getGallery().getGalleryMD5Key());
				String sign = DigestUtils.md5DigestAsHex(signStr.getBytes());
				sbHtml.append(
						"<html> <head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
				sbHtml.append("<form id=\"ebank\" action=\""+platformOrder.getGallery().getReqUrl()+"\" method=\"post\">");
				sbHtml.append("<input type=\"hidden\" name=\"p1_MerchantNo\" value=\"" + merNo + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p2_OrderNo\" value=\"" + platformOrder.getSysOrderNumber() + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p3_Amount\" value=\"" + amount + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p4_Cur\" value=\"1\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p5_ProductName\" value=\"IWANOL\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p7_ReturnUrl\" value=\"" + uri+"/huiju_ebank_ret" + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p8_NotifyUrl\" value=\"" + "AB|" + uri+"/huiju_notify" + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"p9_FrpCode\" value=\"" + paymentType + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"pa_OrderPeriod\" value=\"0\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"hmac\" value="+sign+">");
				// submit按钮控件请不要含有name属性
				sbHtml.append("</form>");
				sbHtml.append("<script>document.forms['ebank'].submit();</script>");
				sbHtml.append("</body></html>");
				logger.info("汇聚网银报文:"+sbHtml.toString());
				response.getWriter().print(sbHtml.toString());
			}else{
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	public void order_shuangqian_ebank(Map<String, String> map, PlatformOrder platformOrder, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException{
		String uri = map.get("uri");
		String service = map.get("service");
		try{
			
			if (service.equals("iwanol.bank.native")){
				String amount = String.format("%.2f", Double.valueOf(map.get("total_fee")) * 0.01);
				String merNo = platformOrder.getGallery().getGalleryAccount();
				Product product = prodcutServiceImpl.findByProductMark(map.get("bank_code"));
				String paymentType = getBankMark(map.get("bank_code"), "shuangqian");
				platformOrder.setProduct(product);
				orderServiceImpl.saveEntity(platformOrder);// 持久化到数据库
				StringBuffer sbHtml = new StringBuffer();
				String signStr = "Amount="+amount+"&BillNo="+platformOrder.getSysOrderNumber()+"&MerNo="+merNo+"&ReturnURL="+uri+"/shuangqian_ebank_ret";
				String sign = MD5Util.getMD5Util().sign(signStr,"&"+MD5Util.getMD5Util().MD5(platformOrder.getGallery().getGalleryMD5Key()).toUpperCase(), "UTF-8").toUpperCase();
				sbHtml.append(
						"<html> <head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
				sbHtml.append("<form id=\"ebank\" action=\""+platformOrder.getGallery().getReqUrl()+"\" method=\"post\">");
				sbHtml.append("<input type=\"hidden\" name=\"MerNo\" value=\"" + merNo + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"BillNo\" value=\"" + platformOrder.getSysOrderNumber() + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"Amount\" value=\"" + amount + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"PayType\" value=\"CSPAY\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"PaymentType\" value=\"" + paymentType + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"ReturnURL\" value=\"" + uri+"/shuangqian_ebank_ret" + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"NotifyURL\" value=\"" + uri+"/shuangqian_ebank_notify" + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"MD5info\" value=\"" + sign + "\"/>");
				sbHtml.append("<input type=\"hidden\" name=\"IsUserCharges\" value=\"0\"/>");
				// submit按钮控件请不要含有name属性
				sbHtml.append("</form>");
				sbHtml.append("<script>document.forms['ebank'].submit();</script>");
				sbHtml.append("</body></html>");
				response.getWriter().print(sbHtml.toString());
			}else{
				request.setAttribute("message", Message.result.ret("10027"));
				request.getRequestDispatcher("/except").forward(request, response);
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.info("下单异常:" + e.getMessage());
			request.setAttribute("message", Message.result.ret("10003"));
			request.getRequestDispatcher("/except").forward(request, response);
		}
	}
	
	public String getBankMark(String bankCode,String flag){
		if (flag.equals("shuangqian")){
			if (bankCode.equals("CCB")) return "CCB";
			if (bankCode.equals("ICBC")) return "ICBC";
			if (bankCode.equals("ABC")) return "ABC";
			if (bankCode.equals("CMB")) return "CMB";
			if (bankCode.equals("GZCB")) return null;
			if (bankCode.equals("COMM")) return "BOCOM";
			if (bankCode.equals("CMBC")) return "CMBC";
			if (bankCode.equals("PSBC")) return "PSBC";
			if (bankCode.equals("BOC")) return "BOCSH";
			if (bankCode.equals("CITIC")) return "CNCB";
			if (bankCode.equals("SPDB")) return "SPDB";
			if (bankCode.equals("CEB")) return "CEB";
			if (bankCode.equals("CIB")) return "CIB";
			if (bankCode.equals("HXB")) return "HXB";
			if (bankCode.equals("PINGAN")) return "PAB";
			if (bankCode.equals("CGB")) return "GDB";
			if (bankCode.equals("BOB")) return "BCCB";
			if (bankCode.equals("BOSC")) return "BOS";
		}	
		if (flag.equals("huiju")){
			if (bankCode.equals("CCB")) return "CCB_NET_B2C";
			if (bankCode.equals("ICBC")) return "ICBC_NET_B2C";
			if (bankCode.equals("ABC")) return "ABC_NET_B2C";
			if (bankCode.equals("CMB")) return "CMBCHINA_NET_B2C";
			if (bankCode.equals("GZCB")) return null;
			if (bankCode.equals("COMM")) return "BOCO_NET_B2C";
			if (bankCode.equals("CMBC")) return null;
			if (bankCode.equals("PSBC")) return "POST_NET_B2C";
			if (bankCode.equals("BOC")) return "BOC_NET_B2C";
			if (bankCode.equals("CITIC")) return "ECITIC_NET_B2C";
			if (bankCode.equals("SPDB")) return "SPDB_NET_B2C";
			if (bankCode.equals("CEB")) return "CEB_NET_B2C";
			if (bankCode.equals("CIB")) return "CIB_NET_B2C";
			if (bankCode.equals("HXB")) return "HXB_NET_B2C";
			if (bankCode.equals("PINGAN")) return "PINGANBANK_NET_B2C";
			if (bankCode.equals("CGB")) return "CGB_NET_B2C";
			if (bankCode.equals("BOB")) return "BCCB_NET_B2C";
			if (bankCode.equals("BOSC")) return "SHB_NET_B2C";
		}
		return null;
	}
}
