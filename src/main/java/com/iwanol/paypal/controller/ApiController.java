package com.iwanol.paypal.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.iwanol.paypal.domain.AccountDetails;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.GalleryProductCost;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.domain.Product;
import com.iwanol.paypal.domain.ProductRate;
import com.iwanol.paypal.domain.SystemSet;
import com.iwanol.paypal.service.impl.BankServiceImpl;
import com.iwanol.paypal.service.impl.GalleryProductCostServiceImpl;
import com.iwanol.paypal.service.impl.MerchantProductGalleryServiceImpl;
import com.iwanol.paypal.service.impl.MerchantServiceImpl;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.service.impl.ProdcutServiceImpl;
import com.iwanol.paypal.service.impl.ProductRateServiceImpl;
import com.iwanol.paypal.service.impl.RouteServiceImpl;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.third.hangtian.ContextUtil;
import com.iwanol.paypal.third.hangtian.HttpUtil;
import com.iwanol.paypal.third.hangtian.MySecurity;
import com.iwanol.paypal.third.hangtian.PPSecurity;
import com.iwanol.paypal.thread.ThreadPool;
import com.iwanol.paypal.unified.Alipay;
import com.iwanol.paypal.unified.Ebank;
import com.iwanol.paypal.unified.Qpay;
import com.iwanol.paypal.unified.Union;
import com.iwanol.paypal.unified.Wechat;
import com.iwanol.paypal.util.Base64Util;
import com.iwanol.paypal.util.CommonUtil;
import com.iwanol.paypal.util.ConnectionUrl;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.Encryption;
import com.iwanol.paypal.util.MD5Util;
import com.iwanol.paypal.util.SHA1;
import com.iwanol.paypal.util.XmlUtil;
import com.iwanol.paypal.vo.Message;
import com.iwanol.paypal.vo.Sms;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@Api(tags = { "API接口" })
@Controller
public class ApiController {
	@Autowired
	private MerchantServiceImpl merchantServiceImpl;
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	@Autowired
	private RouteServiceImpl routeServiceImpl;
	@Autowired
	private Alipay alipay;
	@Autowired
	private Wechat wechat;
	@Autowired
	private Qpay qpay;
	@Autowired
	private Union union;
	@Autowired
	private Ebank ebank;
	@Autowired
	private GalleryProductCostServiceImpl galleryProductCostServiceImpl;
	@Autowired
	private ProductRateServiceImpl productRateServiceImpl;
	@Autowired
	private BankServiceImpl bankServiceImpl;
	@Autowired
	private ProdcutServiceImpl prodcutServiceImpl;
	@Autowired
	private MerchantProductGalleryServiceImpl merchantProductGalleryServiceImpl;
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 下单请求API
	 * 
	 * @param request
	 * @throws IOException
	 */
	@ApiIgnore
	@GetMapping("/gateway")
	public String gateway(HttpServletRequest request, HttpServletResponse response, RedirectAttributes attr)
			throws IOException {
		try {
			response.setHeader("content-type", "text/html;charset=UTF-8");
			String uri = CommonUtil.getCommonUtil().getCurrentDomain(request);
			String data = request.getParameter("data");
			if (StringUtils.isEmpty(data)) {// 判断data是否存在
				attr.addFlashAttribute("message", Message.result.ret("10030"));
				return "redirect:/403";
			} else {
				String param = new String(Base64Util.decode(data));
				logger.info("【下单请求串^_^{}】【请求地址:{}】", param, uri);
				Map<String, String> map = CoreUtil.getCoreUtil().jsonToMap(param);
				JSONObject message = CoreUtil.getCoreUtil().validateInformation(map);
				if (message.getString("status").equals("0")) {// 下单请求参数校验 0 校验成功

					String code = "1";
					if (map.get("service").toString().equals("iwanol.bank.native")
							|| map.get("service").toString().equals("bank.native")) {
						code = map.get("bank_code");
					}
					Set<String> keySet = new HashSet<String>();
					keySet.add("sign");
					// keySet.add("risk");
					String account = map.get("merchant_id").toString();
					Merchant merchant = merchantServiceImpl.findByAccount(account);
					Map<String, Object> validResult = merchantServiceImpl.findMerchantState(account);
					String signStr = CoreUtil.getCoreUtil().formatUrlMap(map, keySet, true, false, true);
					// 对用户参数进行签名认证
					if (!MD5Util.getMD5Util().verify(signStr, map.get("sign"), merchant.getMerchantKey(),
							map.get("charset"))) {
						attr.addFlashAttribute("message", Message.result.ret("10010")); // 签名认证不通过
						return "redirect:/403";
					}
					if (StringUtils.isEmpty(validResult.get("state"))) {
						attr.addFlashAttribute("message", Message.result.ret("10000")); // 商户不存在
						return "redirect:/403";
					}
					if (!"1".equals(validResult.get("state").toString())) {// 判断商户支付功能是否开启
						attr.addFlashAttribute("message", Message.result.ret("10029")); // 支付功能不能使用
						return "redirect:/403";
					}
					if (!map.get("service").contains("iwanol")) {
						map.put("service", "iwanol.".concat(map.get("service")));
					}
					String service = map.get("service");
					// 判断产品是否可用
					Product product = prodcutServiceImpl
							.findByProductMark(CoreUtil.getCoreUtil().serviceToProductMark(service, code));
					if (!product.getState()) {
						attr.addFlashAttribute("message", Message.result.ret("10027")); // 产品不可用
						return "redirect:/403";
					}
					if (orderServiceImpl.findMerchantOrderIsExit(map.get("merchant_order"))) { // 判断订单号是否唯一
						attr.addFlashAttribute("message", Message.result.ret("10002")); // 订单号重复
						return "redirect:/403";
					}
					if (merchant.getMaxAmount().compareTo(BigDecimal.valueOf(0.00)) == 1) {
						if (merchant.getMinAmount()
								.compareTo(BigDecimal.valueOf(Double.valueOf(map.get("total_fee")) * 0.01)) == 1) {
							attr.addFlashAttribute("message",
									Message.result.msg("10044", "下单金额不能低于 " + merchant.getMinAmount() + " 元"));
							return "redirect:/403";
						}
						if (merchant.getMaxAmount()
								.compareTo(BigDecimal.valueOf(Double.valueOf(map.get("total_fee")) * 0.01)) == -1) {
							attr.addFlashAttribute("message",
									Message.result.msg("10045", "下单金额不能大于 " + merchant.getMaxAmount() + " 元"));
							return "redirect:/403";
						}
					}
					// 判断下单所走通道
					BigDecimal amount = BigDecimal.valueOf(Double.valueOf(map.get("total_fee")) * 0.01);
					// 查询下单产品通道路由列表集合
					Gallery gallery = null;
					if (systemSetServiceImpl.findEntitys().get(0).getRouteState() && product.getType() != 1) {// 路由开启
						logger.info("路由已开启【走路由通道】");
						gallery = routeServiceImpl.getGallery(product.getProductMark(), amount);
					}
					if (StringUtils.isEmpty(gallery)) {
						// 走商户默认通道
						logger.info("【下单路由分发^_^:按商户默认通道下单】");
						gallery = merchantProductGalleryServiceImpl
								.findGalleryIdByMerchantIdAndProductId(merchant.getId(), product.getId()).getGallery();
					}
					Map<String, String> url = routeServiceImpl.findReqUrl(gallery.getId(), product.getId());
					if (!StringUtils.isEmpty(url)) {
						gallery.setReqUrl(url.get("url"));
					} else {
						attr.addFlashAttribute("message", Message.result.ret("10039")); // 下单提交域名不存在
						return "redirect:/403";
					}

					map.put("reqParam", param);
					map.put("uri", uri);
					logger.info("下单所走通道:【{}】【提交地址:{}】", gallery.getGalleryName(), uri);
					switch (service) {
					case "iwanol.alipay.native":
						alipay.order(map, request, response, merchant, gallery);
						break; // 支付宝
					case "iwanol.wechat.native":
						wechat.order(map, request, response, merchant, gallery);
						break; // 微信
					case "iwanol.bank.native":
						ebank.order(map, request, response, merchant, gallery);
						break; // 网银
					case "iwanol.cft.native":
						break; // 财付通
					case "alipay.wap.native":
						break; // 支付宝wap
					case "wechat.wap.native":
						break; // 微信wap
					case "iwanol.qpay.native":
						qpay.order(map, request, response, merchant, gallery);
						break; // qq扫码
					case "iwanol.hbpay.native":
						alipay.order(map, request, response, merchant, gallery);
						break; // 花呗
					case "iwanol.ecode.native":
						union.order(map, request, response, merchant, gallery);
						break; // 银联扫码
					case "iwanol.meituan.native":
						break; // 美团扫码
					case "iwanol.dzdp.native":
						break; // 大众点评
					case "iwanol.jingdong.native":
						break; // 京东扫码
					}

				} else {
					attr.addFlashAttribute("message", message);
					return "redirect:/403";
				}
			}
		} catch (Exception e) {
			logger.error("系统异常:{}", e.getMessage());
			attr.addFlashAttribute("message", Message.result.ret("10003"));
			return "redirect:/403";
		}
		return null;
	}

	/**
	 * 向下级商户发送异步通知参数
	 * 
	 * @param platformOrder
	 * @param result
	 * @param response
	 * @throws IOException
	 */
	@ApiIgnore
	public void sendNotifyParam(PlatformOrder platformOrder, String result, HttpServletResponse response)
			throws IOException {
		String notifyUrl = "";
		Map<String, Object> synRetMap = new HashMap<String, Object>();
		String complate = CommonUtil.getCommonUtil().currentDateTime(CommonUtil.getCommonUtil().YMDHFS, new Date());
		BigDecimal amount = platformOrder.getAmount();// 下单金额
		BigDecimal merchantProfit = platformOrder.getMerchantProfits();// 商户利润
		BigDecimal platformProfit = platformOrder.getPlatformProfits();// 平台利润
		BigDecimal agencyProfit = platformOrder.getAgencyProfits(); // 代理利润
		BigDecimal tailProfit = platformOrder.getTailProfit(); // 尾额利润
		BigDecimal tailAmount = platformOrder.getTailAmount(); // 风控金额
		BigDecimal merTailProfit = platformOrder.getMerTailProfit(); // 商户尾额收入
		Merchant merchant = platformOrder.getMerchant();
		Product product = platformOrder.getProduct();
		Gallery gallery = platformOrder.getGallery();
		GalleryProductCost galleryProductCost = galleryProductCostServiceImpl
				.findByGalleryIdAndProductId(gallery.getId(), product.getId());// 通道成本价格
		ProductRate productRate = productRateServiceImpl.findByMerchantIdAndProductId(merchant.getId(),
				product.getId()); // 商户产品价格
		// 封装下发参数
		synRetMap.put("service", CoreUtil.getCoreUtil().productMarkToService(product.getProductMark()));
		synRetMap.put("version", "1.0");
		synRetMap.put("charset", "UTF-8");
		synRetMap.put("sign_type", "MD5");
		synRetMap.put("merchant_id", merchant.getAccount());
		synRetMap.put("trade_order", platformOrder.getSysOrderNumber());
		synRetMap.put("merchant_order", platformOrder.getMerchantOrderNumber());
		synRetMap.put("total_fee", String.format("%.0f", platformOrder.getAmount().doubleValue() * 100));
		synRetMap.put("attach", platformOrder.getAttach() == null ? "" : platformOrder.getAttach());
		synRetMap.put("complate_date", complate);
		synRetMap.put("status", "0");
		synRetMap.put("message", "success");
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(synRetMap, null, true, false, true);
		String sign = MD5Util.getMD5Util().sign(signStr, merchant.getMerchantKey(), "UTF-8");
		synRetMap.put("sign", sign);
		if (!StringUtils.isEmpty(platformOrder.getAgencyAccount())) {
			Merchant agency = merchantServiceImpl.findByAccount(platformOrder.getAgencyAccount());
			merchant.setMerchant(agency);
		}
		if (merchant.getMerchant() != null) {// 判断是否代理下属商户

			ProductRate agencyProductRate = productRateServiceImpl
					.findByMerchantIdAndProductId(merchant.getMerchant().getId(), product.getId()); // 代理商户产品价格

			merchantProfit = amount.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01))).setScale(4,RoundingMode.HALF_UP);

			platformProfit = amount.multiply(galleryProductCost.getPayRate().multiply(BigDecimal.valueOf(0.01)))
					.subtract(amount.multiply(agencyProductRate.getProductRate().multiply(BigDecimal.valueOf(0.01)))).setScale(4,RoundingMode.HALF_UP);

			agencyProfit = amount.multiply(agencyProductRate.getProductRate().multiply(BigDecimal.valueOf(0.01)))
					.subtract(amount.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01)))).setScale(4,RoundingMode.HALF_UP);

			logger.info("提交金额{},代理ID{},代理费率{},代理利润{},商户ID{},商户费率{},商户利润{}", amount, merchant.getMerchant().getId(),
					agencyProductRate.getProductRate(), agencyProfit, merchant.getId(), productRate.getProductRate(), merchantProfit);

		} else {
			merchantProfit = amount.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01))).setScale(4,RoundingMode.HALF_UP);
			platformProfit = amount.multiply(galleryProductCost.getPayRate().multiply(BigDecimal.valueOf(0.01)))
					.subtract(amount.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01)))).setScale(4,RoundingMode.HALF_UP);
		}

		tailProfit = tailAmount.multiply(galleryProductCost.getPayRate().multiply(BigDecimal.valueOf(0.01))).setScale(4,RoundingMode.HALF_UP);

		Integer tailRatio = StringUtils.isEmpty(merchant.getTailRatio()) ? 0 : merchant.getTailRatio();

		if (merchant.getIsOpenTail() && tailRatio > 0) {
			merTailProfit = tailProfit.multiply(new BigDecimal(tailRatio).multiply(new BigDecimal("0.01"))).setScale(4,RoundingMode.HALF_UP);
			tailProfit = tailProfit.multiply(new BigDecimal(100 - tailRatio).multiply(new BigDecimal("0.01"))).setScale(4,RoundingMode.HALF_UP);
			logger.info("风控分成比例：【{}】,商户风控收入【{}】，平台风控收入【{}】", tailRatio, merTailProfit, tailProfit);
		}

		synchronized (this) {
			// 订单
			platformOrder.setMerchantProfits(merchantProfit);
			platformOrder.setPlatformProfits(platformProfit);
			platformOrder.setAgencyProfits(agencyProfit);
			platformOrder.setTailProfit(tailProfit);
			platformOrder.setMerTailProfit(merTailProfit);
			platformOrder.setCompleteDate(
					CommonUtil.getCommonUtil().stringToDate(CommonUtil.getCommonUtil().YMDHFS, complate));
			platformOrder.setState(2);
			// 账户余额
			Bank bank = bankServiceImpl.findByMerchantIdAndBankType(merchant.getId(), true);
			bank.setOverMoney(bank.getOverMoney().add(merchantProfit).add(merTailProfit));
			bank.setAllDeposit(bank.getAllDeposit().add(merchantProfit).add(merTailProfit));

			// 更新代理账户余额
			if (merchant.getMerchant() != null) {
				BigDecimal agencyAmount = agencyProfit;
				Long merId = merchant.getMerchant().getId();
				String sql = "update bank set over_money = over_money + ?,all_deposit = all_deposit + ? where merchant_id = ? and bank_type = 1";
				int i = jdbcTemplate.update(sql, new PreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps) throws SQLException {
						// TODO Auto-generated method stub
						ps.setBigDecimal(1, agencyAmount);
						ps.setBigDecimal(2, agencyAmount);
						ps.setLong(3, merId);
					}
				});
				if (i > 0) {
					logger.info("封装代理{}商户账户余额信息,代理利润{}元", merId, agencyAmount);
				}
			}

			// 账目明细
			AccountDetails accountDetails = new AccountDetails();
			accountDetails.setAmount(amount);
			if (merTailProfit.compareTo(BigDecimal.ZERO) > 0) {
				accountDetails.setDetails("订单:" + platformOrder.getSysOrderNumber() + "支付成功,支付系统将返款 "
						+ String.format("%.2f", (merchantProfit.add(merTailProfit)).doubleValue()) + "元到商户【"
						+ merchant.getAccount() + "】账户,其中风控返利【" + String.format("%.2f", merTailProfit.doubleValue())
						+ "】元");
			} else {
				accountDetails.setDetails("订单:" + platformOrder.getSysOrderNumber() + "支付成功,支付系统将返款 "
						+ String.format("%.2f", (merchantProfit.add(merTailProfit)).doubleValue()) + "元到商户【"
						+ merchant.getAccount() + "】账户");
			}
			accountDetails.setMerchant(merchant);
			accountDetails.setRecordDate(new Date());
			accountDetails.setRecordNumber(platformOrder.getSysOrderNumber());
			accountDetails.setType(true);
			// 更新订单，账户余额，账目明细
			orderServiceImpl.opration(platformOrder, bank, accountDetails);
			logger.info("【订单{}支付成功,订单状态、账户余额、账目明细 更新成功】", platformOrder.getSysOrderNumber());
			// 向商户转发成功后的订单信息
			if (StringUtils.isEmpty(merchant.getNotifyUrl())) {
				notifyUrl = platformOrder.getNotifyUrl();
			} else {
				notifyUrl = merchant.getNotifyUrl();
			}
			new ThreadPool(platformOrder, notifyUrl, synRetMap, orderServiceImpl).cahcheThreadPool();
		}
		if (gallery.getGalleryMark().contains("hangtian")) {
			response.setContentLength(result.getBytes("utf-8").length);
			response.getOutputStream().write(result.getBytes("utf-8"));
			response.getOutputStream().close();
		} else {
			response.getWriter().print(result);
		}
	}

	/**
	 * 支付跳转成功页面
	 */
	@ApiIgnore
	@GetMapping("/success.html")
	public ModelAndView paySuccess(HttpServletRequest request, PlatformOrder platformOrder)
			throws UnsupportedEncodingException {
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		Map<String, Object> retMap = new HashMap<String, Object>();
		String uri = ""; // 获取当前域名
		Integer port = request.getServerPort(); // 获取当前端口
		String scheme = request.getScheme(); // 获取通信协议
		if (port == 80) {
			uri = scheme + "://" + request.getServerName() + "/404";
		} else {
			uri = scheme + "://" + request.getServerName() + ":" + port + "/404";
		}
		jsonMap.put("platformOrderNumber", platformOrder.getSysOrderNumber()); // 获取平台订单号
		jsonMap.put("orderMoney", platformOrder.getAmount()); // 获取订单金额
		jsonMap.put("orderNumber", platformOrder.getMerchantOrderNumber());
		jsonMap.put("productName", platformOrder.getProductName());
		jsonMap.put("servicePhone", systemSet.getServicePhone().isEmpty() ? "--" : systemSet.getServicePhone());
		jsonMap.put("serviceQQ", systemSet.getServiceQQ().isEmpty() ? "--" : systemSet.getServiceQQ());
		// 同步发送消息给商户
		String service = CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProductMark());
		if (service.contains("iwanol.")) {
			service = service.replace("iwanol.", "");
		}
		retMap.put("service", service);
		retMap.put("version", "1.0");
		retMap.put("charset", "UTF-8");
		retMap.put("sign_type", "MD5");
		retMap.put("status", "0");
		retMap.put("message", "success");
		retMap.put("merchant_id", platformOrder.getMerchantAccount());
		retMap.put("total_fee", String.format("%.0f", platformOrder.getAmount().doubleValue() * 100));
		retMap.put("merchant_order", platformOrder.getMerchantOrderNumber());
		retMap.put("trade_no", platformOrder.getSysOrderNumber());
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(retMap, null, true, false, true);
		String sign = MD5Util.getMD5Util().sign(signStr, platformOrder.getMerchantKey(), "UTF-8");
		retMap.put("sign", sign);
		String retStr = CoreUtil.getCoreUtil().formatUrlMap(retMap, null, true, false, true);
		if (!StringUtils.isEmpty(platformOrder.getRetUrl())) {
			uri = platformOrder.getRetUrl();
		}
		jsonMap.put("returnUrl", uri + "?" + retStr);
		ModelAndView mv = new ModelAndView("scan/success", "jsonMap", jsonMap);
		return mv;
	}

	@ApiIgnore
	@GetMapping("/redirect_success")
	public String redirect_success(HttpServletRequest request, RedirectAttributes attr) {
		String sysOrder = request.getParameter("trade_no");
		PlatformOrder platformOrder = orderServiceImpl.findSelectivBySysOrder(sysOrder);
		attr.addFlashAttribute("platformOrder", platformOrder);
		return "redirect:/success.html";
	}

	/**
	 * 订单查询
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@ApiOperation(value = "订单状态查询", notes = "订单状态查询")
	@ApiImplicitParam(name = "trade_no", value = "系统订单号", readOnly = true, paramType = "query", dataType = "String")
	@ApiResponses({ @ApiResponse(code = 1, message = "支付成功"), @ApiResponse(code = 0, message = "未支付"), })
	@PostMapping("/query_order")
	public void queryOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String trade_no = request.getParameter("trade_no"); // 获取系统订单号
		String res = null;// 0表示未支付，1表示已支付
		Map<String, Integer> staMap = orderServiceImpl.findStateBySysOrder(trade_no);
		if (staMap.get("state") == 1 || staMap.get("state") == 2) {
			res = "1";
		} else {
			res = "0";
		}
		if (res.startsWith("<")) {
			response.setHeader("Content-type", "text/xml;charset=UTF-8");
		} else {
			response.setHeader("Content-type", "text/html;charset=UTF-8");
		}
		response.getWriter().print(res);
	}

	@PostMapping("/client_ip")
	@ResponseBody
	public Map<String, Object> clientIp(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();
		orderServiceImpl.updateClientIp(request);
		map.put("code", 1);
		return map;
	}

	@ApiOperation(value = "生成二维码", notes = "生成二维码")
	@ApiImplicitParam(name = "url", value = "二维码加密url", required = true, paramType = "query", dataType = "String")
	@GetMapping("/ercode")
	public void ercode(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			String url = request.getParameter("url");
			CoreUtil.getCoreUtil().encoderQRCoder(Encryption.getEncryption().aesDeCrypt(url), response);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * 订单补发
	 * 
	 * @param merchantOrder
	 * @throws IOException
	 */
	@ApiOperation(value = "订单补发", notes = "<p>订单补发验签规则:</p>" + "<p>sign=md5(商户订单号+金额+key)</p>")
	@ApiResponses({ @ApiResponse(code = 10033, message = "该订单是成功订单,补发网关积极拒绝"),
			@ApiResponse(code = 10034, message = "未支付成功订单,补发网关积极拒绝"), @ApiResponse(code = 10035, message = "订单补发成功"),
			@ApiResponse(code = 10036, message = "订单不存在,补发网关积极拒绝"),
			@ApiResponse(code = 10037, message = "补发失败,目标服务器积极拒绝") })
	@GetMapping("/send")
	public void send(@RequestParam(value = "merchantOrder") String merchantOrder,
			@RequestParam(value = "sign") String yqsign, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding("utf-8");
		PlatformOrder platformOrder = orderServiceImpl.findByMerchantOrder(merchantOrder);
		String yqStr = merchantOrder + String.format("%.2f", platformOrder.getAmount());
		if (MD5Util.getMD5Util().verify(yqStr, yqsign, platformOrder.getMerchant().getMerchantKey(), "utf-8")) {
			if (!StringUtils.isEmpty(platformOrder)) {
				Map<String, Object> synRetMap = new HashMap<String, Object>();
				String complate = CommonUtil.getCommonUtil().currentDateTime(CommonUtil.getCommonUtil().YMDHFS,
						new Date());
				// 封装下发参数
				synRetMap.put("service",
						CoreUtil.getCoreUtil().productMarkToService(platformOrder.getProduct().getProductMark()));
				synRetMap.put("version", "1.0");
				synRetMap.put("charset", "UTF-8");
				synRetMap.put("sign_type", "MD5");
				synRetMap.put("merchant_id", platformOrder.getMerchant().getAccount());
				synRetMap.put("trade_order", platformOrder.getSysOrderNumber());
				synRetMap.put("merchant_order", platformOrder.getMerchantOrderNumber());
				synRetMap.put("total_fee", String.format("%.0f", platformOrder.getAmount().doubleValue() * 100));
				synRetMap.put("attach", platformOrder.getAttach() == null ? "" : platformOrder.getAttach());
				synRetMap.put("complate_date", complate);
				synRetMap.put("status", "0");
				synRetMap.put("message", "success");
				String signStr = CoreUtil.getCoreUtil().formatUrlMap(synRetMap, null, true, false, true);
				String sign = MD5Util.getMD5Util().sign(signStr, platformOrder.getMerchant().getMerchantKey(), "UTF-8");
				synRetMap.put("sign", sign);
				if (platformOrder.getState() == 1) {
					response.getWriter().print(Message.result.ret("10033"));
				} else {
					String notifyUri = "";
					if (StringUtils.isEmpty(platformOrder.getMerchant().getNotifyUrl())) {
						notifyUri = platformOrder.getNotifyUrl();
					} else {
						notifyUri = platformOrder.getMerchant().getNotifyUrl();
					}
					if (notifyUri.contains("?")) {
						notifyUri = notifyUri + "&"
								+ CoreUtil.getCoreUtil().formatUrlMap(synRetMap, null, false, false, false);
					} else {
						notifyUri = notifyUri + "?"
								+ CoreUtil.getCoreUtil().formatUrlMap(synRetMap, null, false, false, false);
					}
					logger.info("订单补发链接:{}", notifyUri);
					String result = ConnectionUrl.getConnectionUrl().httpRequest(notifyUri, "GET", null,
							synRetMap.get("charset").toString());
					logger.info("订单补发商户响应信息:{}", result);
					if (result.contains("success")) {
						if (platformOrder.getState() == 0) {// 疑单处理
							BigDecimal amount = platformOrder.getAmount();// 下单金额
							BigDecimal merchantProfit = platformOrder.getMerchantProfits();// 商户利润
							BigDecimal platformProfit = platformOrder.getPlatformProfits();// 平台利润
							BigDecimal agencyProfit = platformOrder.getAgencyProfits(); // 代理利润
							GalleryProductCost galleryProductCost = galleryProductCostServiceImpl
									.findByGalleryIdAndProductId(platformOrder.getGallery().getId(),
											platformOrder.getProduct().getId());// 通道成本价格
							ProductRate productRate = productRateServiceImpl.findByMerchantIdAndProductId(
									platformOrder.getMerchant().getId(), platformOrder.getProduct().getId()); // 商户产品价格
							if (platformOrder.getMerchant().getMerchant() != null) {// 判断是否代理下属商户
								ProductRate agencyProductRate = productRateServiceImpl.findByMerchantIdAndProductId(
										platformOrder.getMerchant().getMerchant().getId(),
										platformOrder.getProduct().getId()); // 代理商户产品价格
								merchantProfit = amount
										.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01)));
								platformProfit = amount
										.multiply(galleryProductCost.getPayRate().multiply(BigDecimal.valueOf(0.01)))
										.subtract(amount.multiply(
												agencyProductRate.getProductRate().multiply(BigDecimal.valueOf(0.01))));
								agencyProfit = amount
										.multiply(agencyProductRate.getProductRate().multiply(BigDecimal.valueOf(0.01)))
										.subtract(amount.multiply(
												productRate.getProductRate().multiply(BigDecimal.valueOf(0.01))));
							} else {
								merchantProfit = amount
										.multiply(productRate.getProductRate().multiply(BigDecimal.valueOf(0.01)));
								platformProfit = amount
										.multiply(galleryProductCost.getPayRate().multiply(BigDecimal.valueOf(0.01)))
										.subtract(amount.multiply(
												productRate.getProductRate().multiply(BigDecimal.valueOf(0.01))));
							}
							platformOrder.setState(1);
							platformOrder.setMerchantProfits(merchantProfit);
							platformOrder.setPlatformProfits(platformProfit);
							platformOrder.setAgencyProfits(agencyProfit);
							platformOrder.setCompleteDate(CommonUtil.getCommonUtil()
									.stringToDate(CommonUtil.getCommonUtil().TIMESTAMP, CommonUtil.getCommonUtil()
											.currentDateTime(CommonUtil.getCommonUtil().TIMESTAMP, new Date())));
							// 更新当前商户账户余额
							Bank bank = bankServiceImpl.findByMerchantIdAndBankType(platformOrder.getMerchant().getId(),
									true);
							bank.setOverMoney(bank.getOverMoney().add(merchantProfit));
							bank.setAllDeposit(bank.getAllDeposit().add(merchantProfit));
						} else {
							platformOrder.setState(1);
							platformOrder.setCompleteDate(CommonUtil.getCommonUtil()
									.stringToDate(CommonUtil.getCommonUtil().TIMESTAMP, CommonUtil.getCommonUtil()
											.currentDateTime(CommonUtil.getCommonUtil().TIMESTAMP, new Date())));
						}
						orderServiceImpl.updateEntity(platformOrder);
						response.getWriter().print(Message.result.ret("10035"));
					} else {
						response.getWriter().print(Message.result.ret("10037"));
					}
				}
			} else {
				response.getWriter().print(Message.result.ret("10036"));
			}
		} else {
			response.getWriter().print(Message.result.ret("10010"));
		}
	}

	/**
	 * 
	 * @return 腾讯云短信接口
	 * @throws UnsupportedEncodingException
	 */
	@ApiOperation(value = "腾讯云短信接口", notes = "腾讯云短信接口")
	@GetMapping("/sms")
	@ResponseBody
	public String sms(Sms sms) throws UnsupportedEncodingException {
		String msg = "-1";
		try {
			ArrayList<String> lists = new ArrayList<String>();
			// 初始化腾讯云单发模版
			SmsSingleSender singleSender = new SmsSingleSender(sms.getAppId(), sms.getAppKey());
			if (sms.getType().equals("yzm")) {
				lists.add(sms.getParamOne());
				lists.add(sms.getParamTwo());
				SmsSingleSenderResult singleSenderResult = singleSender.sendWithParam("86", sms.getTelPhone(),
						sms.getTemplateId(), lists, "", "", "");
				if (singleSenderResult.result == 0) { // 成功
					msg = Message.result.ret("10042");
				} else {
					msg = Message.result.ret("10043");
				}
			}
		} catch (Exception e) {
			msg = Message.result.ret("10043");
		}
		return msg;
	}

	/**
	 * 订单查询接口
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/order/query")
	@ResponseBody
	public Map<String, String> orderQuery(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> param = new HashMap<String, String>();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> ret = new HashMap<String, String>();
		Map<String, Object> order = new HashMap<String, Object>();
		Map<String, String> u = new HashMap<String, String>();
		String service = request.getParameter("service");
		String signType = request.getParameter("sign_type");
		String merchantId = request.getParameter("merchant_id");
		String orderNo = request.getParameter("order_no");
		String sign = request.getParameter("sign");
		map.put("merchantId", merchantId);
		try {
			order = orderServiceImpl.findOrderByMerchantOrderNumber(orderNo);
		} catch (Exception e) {
			u.put("retCode", "0");
			u.put("retMsg", "system error!");
			return u;
		}
		map.put("orderNo", orderNo);
		// 参数封装
		param.put("service", service);
		param.put("sign_type", signType);
		param.put("merchant_id", merchantId);
		param.put("order_no", orderNo);
		String key = merchantServiceImpl.findKeyByAccount(merchantId).get("key");
		String signStr = CoreUtil.getCoreUtil().formatUrlMap(param, null, true, false, true);
		if (MD5Util.getMD5Util().verify(signStr, sign, "&key=" + key, "utf-8")) {
			try {
				if (ret != null) {
					ret.put("service", service);
					ret.put("sign_type", signType);
					ret.put("retCode", "1");
					ret.put("order_no", order.get("order_no").toString());
					ret.put("platform_no", order.get("platform_no").toString());
					ret.put("order_amout", order.get("order_amout").toString());
					ret.put("order_date", CommonUtil.getCommonUtil()
							.currentDateTime(CommonUtil.getCommonUtil().TIMESTAMP, (Date) order.get("order_date")));
					ret.put("status", order.get("status").toString());
					ret.put("sign", MD5Util.getMD5Util().sign(
							CoreUtil.getCoreUtil().formatUrlMap(ret, null, true, false, true), "&key=" + key, "utf-8"));
				}
			} catch (Exception e) {
				u.put("retCode", "0");
				u.put("retMsg", "system error!");
				return u;
			}
		} else {
			ret.put("retCode", "0");
			ret.put("retMsg", "sign error！");
		}
		return ret;
	}

	@GetMapping("/callback/MP_verify_WwtAsLyc29IM85J3.txt")
	public String wxMp() {
		return "scan/MP_verify_WwtAsLyc29IM85J3.txt";
	}

	@GetMapping("/wx/gzh")
	public void wxTokenVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String token = "f3de64e0edc54e759dd9bc0cc0908574";
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		ArrayList<String> values = new ArrayList<String>();
		values.add(token);
		values.add(timestamp);
		values.add(nonce);
		String str = values.stream().filter(s -> s.length() > 0).sorted().collect(Collectors.joining());
		logger.info("公众号token校验字典排序串:{}", str);
		if (signature.equals(SHA1.encode(str))) {
			logger.info("微信公众号token校验成功");
			response.getWriter().println(echostr);
		} else {
			logger.error("微信公众号token校验失败,signature【{}】,resign【{}】", signature, SHA1.encode(str));
		}
	}

	@GetMapping("/callback/{type}")
	public ModelAndView hangtianWxGzhPay(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "type") String type, Model model) throws Exception {
		String uri = CommonUtil.getCommonUtil().getCurrentDomain(request);
		String code = request.getParameter("code");
		String orderNumber = request.getParameter("state");
		PlatformOrder order = orderServiceImpl.findBySysOrder(orderNumber);
		Gallery gallery = order.getGallery();
		// 通过code获取access_token
		StringBuffer access_token_req = new StringBuffer();
		access_token_req.append("appid=" + gallery.getAppId()).append("&secret=" + gallery.getGalleryMD5Key())
				.append("&code=" + code).append("&grant_type=authorization_code");
		String access_token_json = HttpRequest.post("https://api.weixin.qq.com/sns/oauth2/access_token")
				.body(access_token_req.toString()).execute().body();
		logger.info("获取微信access_token响应结果:{}", access_token_json);
		JSONObject result = JSONObject.parseObject(access_token_json);
		String openid = result.getString("openid");
		switch (type) {
		case "hangtian":
			return hangtian(order, openid, uri, model);

		default:
			return null;
		}
	}

	public ModelAndView hangtian(PlatformOrder order, String openid, String uri, Model model) throws Exception {
		PPSecurity ppSecurity = MySecurity.getPPSecurity();
		Map<String, String> reqData = new HashMap<String, String>();
		reqData.put("opcode", "MA5001");
		reqData.put("productid", "WX_JSAPI0201");
		reqData.put("subappid", order.getGallery().getAppId());
		reqData.put("subopenid", openid);
		reqData.put("merorderno", order.getSysOrderNumber());
		reqData.put("merorderdate", CommonUtil.getCommonUtil().currentDateTime("yyyyMMdd", new Date()));
		reqData.put("meruserid", UUID.fastUUID().toString(true));
		reqData.put("tranamt",
				String.format("%.0f", (order.getAmount().add(order.getTailAmount())).doubleValue() * 100));
		reqData.put("orderdesc", "QQ:177752992(" + order.getSysOrderNumber() + ")");
		reqData.put("notifyurl", uri + "/hangtian_notify");
		reqData.put("paystyle", "13");
		String xml = XmlUtil.map2Xml(reqData);
		logger.info("下单请求xml:{}", xml);
		byte[] encodeDataByte = ppSecurity.PPFCommDataEncode(xml.getBytes("gbk"));
		xml = new String(encodeDataByte, "gbk");
		xml = URLEncoder.encode(xml, "gbk");
		String merid = ContextUtil.getValue("merid");
		String data = "merid=" + merid + "&transdata=" + xml;
		logger.info("最终请求数据:{}", data);
		String reqUrl = routeServiceImpl.findReqUrl(order.getGallery().getId(), order.getProduct().getId()).get("url");
		String res = HttpUtil.request(reqUrl, data, true);
		// 加密响应数据
		byte[] resB = ppSecurity.PPFCommDataDecode(res.getBytes("gbk"));
		logger.info("航天下单响应参数:{}", new String(resB, "gbk"));
		Map<String, String> ret = XmlUtil.toMap(resB, "gbk");
		logger.info("xml转Map:", ret);
		Map<String, String> jsonMap = new HashMap<String, String>();
		if (ret.get("rspcode").equals("000000")) {
			JSONObject json = JSONObject.parseObject(ret.get("paydata"));
			json.put("amount", String.format("%.2f", order.getAmount()));
			json.put("orderNumber", order.getSysOrderNumber());
			ModelAndView mv = new ModelAndView("scan/h5", "jsonMap", json);
			return mv;
		} else {
			jsonMap.put("message", ret.get("rspdesc"));
			jsonMap.put("status", "10012");
			ModelAndView mv = new ModelAndView("error/403", "message", jsonMap);
			return mv;
		}
	}
}
