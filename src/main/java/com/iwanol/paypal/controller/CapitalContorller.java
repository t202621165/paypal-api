package com.iwanol.paypal.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.vo.Message;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags={"退款接口 Controller"})
@Controller
@RequestMapping("/api")
public class CapitalContorller {
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	private final String refundUrl = "https://mapi.alipay.com/gateway.do";
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 退款接口
	 * 
	 * @param request
	 * @param reponse
	 * @throws Exception
	 */
	@ApiOperation(value="退款接口",notes="退款接口返回的message请url转码<br>"
			+ "<p>验签规则:</p>"
			+ "将参数按字母assic码从小到大排序:sign=md5(排序串+key)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "party_order_number",value="第三方订单号",dataType="String",paramType="query",required=true),
		@ApiImplicitParam(name = "sys_order_number",value="平台订单号",dataType="String",paramType="query",required=true),
		@ApiImplicitParam(name = "discription",value="退款描述",dataType="String",paramType="query",required=false),
		@ApiImplicitParam(name = "sign",value="验签结果",dataType="String",paramType="query",required=true)
	})
	@ApiResponses({
		@ApiResponse(code = 10010,message = "商户签名认证错误!"),
		@ApiResponse(code = 10026,message = "下单提交参数变量名错误!"),
		@ApiResponse(code = 10040,message = "该笔订单不存在,拒绝退款操作!"),
		@ApiResponse(code = 10041,message = "网关接收退款请求成功!"),
	})
	@GetMapping("/refund")
	public void subStitute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("utf-8");
		response.setHeader("content-type", "application/json;charset=UTF-8");
		String uri = CommonUtil.getCommonUtil().getCurrentDomain(request);
		Map<String, Object> params = CoreUtil.getCoreUtil().getParams(request);
		logger.info("【退款请求参数:"+CoreUtil.getCoreUtil().formatUrlMap(params,null,false, false, false)+"】");
		if (params.containsKey("sys_order_number") && params.containsKey("sign") && params.containsKey("party_order_number")){
			Set<String> removeKeys = new HashSet<String>();
			removeKeys.add("sign");
			removeKeys.add("sign_type");
			PlatformOrder platformOrder = orderServiceImpl.findBySysOrder(params.get("sys_order_number").toString());
			if (platformOrder != null) {
				String discription = "协商退款";
				if (params.containsKey("discription")) {
					discription = URLDecoder.decode(params.get("discription").toString(), "UTF-8");
					params.put("discription",discription);
				}
				if (MD5Util.getMD5Util().verify(
						CoreUtil.getCoreUtil().formatUrlMap(params, removeKeys, true, false, true),
						params.get("sign").toString(), platformOrder.getGallery().getGalleryMD5Key(), "utf-8")) {
					// 封装退款请求参数
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("service", "refund_fastpay_by_platform_pwd");
					data.put("partner", platformOrder.getGallery().getGalleryAccount());
					data.put("seller_user_id",platformOrder.getGallery().getGalleryAccount());
					data.put("_input_charset", "UTF-8");
					data.put("sign_type", "MD5");
					data.put("notify_url", uri + "/refund");
					data.put("refund_date", CommonUtil.getCommonUtil()
							.currentDateTime(CommonUtil.getCommonUtil().TIMESTAMP, new Date()));
					data.put("batch_no",CommonUtil.getCommonUtil().currentDateTime("yyyyMMdd",new Date())+CommonUtil.getCommonUtil().random(6));
					data.put("batch_num", "1");
					data.put("detail_data", params.get("party_order_number").toString() + "^"
							+ String.format("%.2f", platformOrder.getAmount()) + "^" + discription);
					String signStr = CoreUtil.getCoreUtil().formatUrlMap(data, removeKeys, true, false, true);
					data.put("sign", MD5Util.getMD5Util().sign(signStr,
							platformOrder.getGallery().getGalleryMD5Key(), "UTF-8"));
					// 退款更新入库
					platformOrder.setState(3);// 退款处理中
					platformOrder.setPartyOrderNumber(params.get("party_order_number").toString());
					platformOrder.setOrderDiscription(discription);
					orderServiceImpl.updateEntity(platformOrder);
					data.put("detail_data",
							URLEncoder.encode(
									params.get("party_order_number").toString() + "^"
											+ String.format("%.2f", platformOrder.getAmount()) + "^" + discription,
									"Utf-8"));
					String url = refundUrl+"?"+CoreUtil.getCoreUtil().formatUrlMap(data, null, true, false, true);
					response.getWriter().print(Message.result.ret("10041", "alipay.refund", url));

				} else {
					response.getWriter().print(Message.result.ret("10010", "alipay.refund"));
				}
			} else {
				response.getWriter().print(Message.result.ret("10040", "alipay.refund"));
			}
		} else {
			response.getWriter().print(Message.result.ret("10026", "alipay.refund"));
		}
	}
}
