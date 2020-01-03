package com.iwanol.paypal.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.vo.Message;
import com.swetake.util.Qrcode;

public class CoreUtil {

	private static CoreUtil coreUtil;

	public static CoreUtil getCoreUtil() {
		if (coreUtil == null) {
			coreUtil = new CoreUtil();
			return coreUtil;
		} else {
			return coreUtil;
		}
	}

	/**
	 * 将json字符串转换成Map String, Object
	 * 
	 * @param String
	 *            json
	 * @return map String, Object
	 */
	@SuppressWarnings("unchecked")
	public <v> Map<String, v> jsonToMap(String json) {
		Map<String, v> result = new HashMap<String, v>();
		try {
			JSONObject object = JSON.parseObject(json);
			Set<String> KeySets = object.keySet();
			// 遍历jsonObject数据，添加到Map对象
			for (String key : KeySets) {
				if (!StringUtils.isEmpty(object.get(key))){
					v value = (v) object.get(key);
					result.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 校验基础参数
	 * 
	 * @param map
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public JSONObject validateInformation(Map<String, String> map) throws UnsupportedEncodingException, IOException {
		Set<String> setKeys = new HashSet<String>();
		setKeys.add("service");
		setKeys.add("version");
		setKeys.add("charset");
		setKeys.add("sign_type");
		setKeys.add("merchant_id");
		setKeys.add("merchant_order");
		setKeys.add("bank_code");
		setKeys.add("attach");
		setKeys.add("total_fee");
//		setKeys.add("notify_url");
//		setKeys.add("return_url");
		setKeys.add("page_type");
		setKeys.add("sign");
		// 下单参数校验
		if (StringUtils.isEmpty(map.get("service"))) {
			return JSONObject.parseObject(Message.result.ret("10025"));
		} else {
			String service = map.get("service");
			if (!map.get("version").equals("1.0")) {
				JSONObject.parseObject(Message.result.ret("10022"));
			} else if (!map.get("charset").equalsIgnoreCase("UTF-8")) {
				JSONObject.parseObject(Message.result.ret("10023"));
			} else if (!map.get("sign_type").equalsIgnoreCase("MD5")) {
				JSONObject.parseObject(Message.result.ret("10024"));
			} else if (!service.equals("iwanol.bank.native")) {// 网银校验
				setKeys.remove("bank_code");
			}
			for (String key : map.keySet()) {
				if (!setKeys.contains(key)) {
					JSONObject.parseObject(Message.result.ret("10026"));
					break;
				}
			}
		}
		return JSONObject.parseObject(Message.result.ret("0"));
	}

	/**
	 * code = 1 为普通产品 否则为网银产品 service转productMark
	 */
	public String serviceToProductMark(String service, String code) {
		String productMark = "-1";
		if (code.equals("1")) {
			switch(service){
				case "iwanol.alipay.native" : productMark = "alipay"; break;
				case "alipay.wap.native" : productMark = "h5_alipay"; break;
				case "iwanol.wechat.native" : productMark = "wechat"; break;
				case "wechat.wap.native" : productMark = "h5_wechat"; break;
				case "iwanol.cft.native" : productMark = "tenpay"; break;
				case "iwanol.qpay.native" : productMark = "qpay"; break;
				case "iwanol.hbpay.native" : productMark = "hbpay"; break;
				case "iwanol.ecode.native" : productMark = "ecode"; break;
				case "iwanol.dzdp.native" : productMark = "ecode"; break;
				case "iwanol.meituan.native" : productMark = "ecode"; break;
				case "iwanol.jingdong.native" : productMark = "ecode"; break;
			}
		}else{
			if(service.equals("iwanol.bank.native")){
				 productMark = code;
			}
		} 
		return productMark;
	}

	/**
	 * 产品mark转service
	 * 
	 * @param productMark
	 * @return
	 */
	public String productMarkToService(String productMark) {
		if (productMark.equals("alipay")) {
			return "iwanol.alipay.native";
		} else if (productMark.equals("h5_alipay")) {
			return "alipay.wap.native";
		} else if (productMark.equals("wechat")) {
			return "iwanol.wechat.native";
		} else if (productMark.equals("h5_wechat")) {
			return "wechat.wap.native";
		} else if (productMark.equals("tenpay")) {
			return "iwanol.cft.native";
		} else if (productMark.equals("qpay")) {
			return "iwanol.qpay.native";
		} else if (productMark.equals("hbpay")) {
			return "iwanol.hbpay.native";
		} else if (productMark.equals("hbpay")) {
			return "iwanol.hbpay.native";
		} else if (productMark.equals("ecode")) {
			return "iwanol.ecode.native";
		} else {
			return "iwanol.bank.native";
		}
	}

	/**
	 * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
	 * 实现步骤: <br>
	 * 
	 * @param paraMap
	 *            要排序的Map对象
	 * @param removeKeys
	 *            需要去除的key
	 * @param urlEncode
	 *            是否需要URLENCODE
	 * @param removeNull
	 *            是否需要去除空值
	 * @return
	 */
	public <v> String formatUrlMap(Map<String, v> paraMap, Set<String> removeKeys, boolean sort, boolean urlEncode,
			boolean removeNull) {
		String buff = "";
		Map<String, v> tmpMap = paraMap;
		try {
			List<Map.Entry<String, v>> infoIds = new ArrayList<Map.Entry<String, v>>(tmpMap.entrySet());
			if (sort) {
				// 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
				Collections.sort(infoIds, new Comparator<Map.Entry<String, v>>() {
					@Override
					public int compare(Map.Entry<String, v> o1, Map.Entry<String, v> o2) {
						return (o1.getKey()).toString().compareTo(o2.getKey());
					}
				});
			}
			// 构造URL 键值对的格式
			StringBuilder buf = new StringBuilder();
			for (Map.Entry<String, v> item : infoIds) {
				if (!StringUtils.isEmpty(item.getKey())) {
					String key = item.getKey();
					String val = item.getValue() == null ? "":item.getValue().toString();
					if (removeNull) {
						if (val == null || "".equals(val)) {
							continue;
						}
					}
					if (removeKeys != null && removeKeys.contains(key)) {
						continue;
					}
					if (urlEncode) {
						val = URLEncoder.encode(val, "utf-8");
					}
					buf.append(key + "=" + val);
					buf.append("&");
				}

			}
			buff = buf.toString();
			if (buff.isEmpty() == false) {
				buff = buff.substring(0, buff.length() - 1);
			}
		} catch (Exception e) {
			return null;
		}
		return buff;
	}

	/**
	 * 通用排序方式
	 */
	public <v> String formatUrlMapToTy(String type, String flag, Map<String, v> map) {
		StringBuffer bf = new StringBuffer();
		if (type.equals("51PayZfb")) {
			if (flag.equals("req")) {
				bf.append("customerid=" + map.get("customerid"));
				bf.append("&sdcustomno=" + map.get("sdcustomno"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&cardno=" + map.get("cardno"));
				bf.append("&faceno=" + map.get("faceno"));
				bf.append("&noticeurl=" + map.get("noticeurl"));
			} else if (flag.equals("notify_resign")) {
				bf.append("sign=" + map.get("sign"));
				bf.append("&customerid=" + map.get("customerid"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&sd51no=" + map.get("sd51no"));
				bf.append("&state=" + map.get("state"));
			}
		} else if (type.equals("51PayDk")) {
			if (flag.equals("req")) {
				bf.append("customerid=" + map.get("customerid"));
				bf.append("&sdcustomno=" + map.get("sdcustomno"));
				bf.append("&noticeurl=" + map.get("noticeurl"));
				bf.append("&mark=" + map.get("mark"));
			} else if (flag.equals("notify")) {
				bf.append("customerid=" + map.get("customerid"));
				bf.append("&sd51no=" + map.get("sd51no"));
				bf.append("&sdcustomno=" + map.get("sdcustomno"));
				bf.append("&mark=" + map.get("mark"));
			}
		} else if (type.equals("70PayDk")) {
			if (flag.equals("req")) {
				bf.append("userid=" + map.get("userid"));
				bf.append("&orderno=" + map.get("orderno"));
				bf.append("&typeid=" + map.get("typeid"));
				bf.append("&cardno=" + map.get("cardno"));
				bf.append("&encpwd=" + map.get("encpwd"));
				bf.append("&cardpwd=" + map.get("cardpwd"));
				bf.append("&cardpwdenc=");
				bf.append("&money=" + map.get("money"));
				bf.append("&url=" + map.get("url"));
			} else if (flag.equals("notify")) {
				bf.append("returncode=" + map.get("returncode"));
				bf.append("&yzchorderno=" + map.get("yzchorderno"));
				bf.append("&userid=" + map.get("userid"));
				bf.append("&orderno=" + map.get("orderno"));
				bf.append("&realmoney=" + map.get("realmoney"));
			}
		} else if (type.equals("51PayWX")) {
			if (flag.equals("req")) {// req-sign
				bf.append("customerid=" + map.get("customerid")).append("&sdcustomno=" + map.get("sdcustomno"))
						.append("&orderAmount=" + map.get("orderAmount")).append("&cardno=" + map.get("cardno"))
						.append("&noticeurl=" + map.get("noticeurl")).append("&backurl=" + map.get("backurl"));
			} else if (flag.equals("notify")) {// notify-resign
				bf.append("sign=" + map.get("sign"));
				bf.append("&customerid=" + map.get("customerid"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&sd51no=" + map.get("sd51no"));
				bf.append("&state=" + map.get("state"));
			} else {// back-sign
				bf.append("sdcustomno=" + map.get("sdcustomno"));
				bf.append("&state=" + map.get("state"));
				bf.append("&sd51no=" + map.get("sd51no"));
			}
		} else if (type.equals("51PayWy")) {
			if (flag.equals("req")) {
				bf.append("customerid=" + map.get("customerid"));
				bf.append("&sdcustomno=" + map.get("sdcustomno"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&cardno=" + map.get("cardno"));
				bf.append("&faceno=" + map.get("faceno"));
			} else if (flag.equals("notify")) {
				bf.append("sign=" + map.get("sign"));
				bf.append("&customerid=" + map.get("customerid"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&sd51no=" + map.get("sd51no"));
				bf.append("&state=" + map.get("state"));
			}
		} else if (type.equals("51PayCft")) {
			if (flag.equals("req")) {
				bf.append("customerid=" + map.get("customerid"));
				bf.append("&sdcustomno=" + map.get("sdcustomno"));
				bf.append("&ordermoney=" + map.get("ordermoney"));
				bf.append("&cardno=" + map.get("cardno"));
				bf.append("&faceno=" + map.get("faceno"));
				bf.append("&noticeurl=" + map.get("noticeurl"));
				bf.append("&endcustomer=" + map.get("endcustomer"));
				bf.append("&endip=" + map.get("endip"));
				bf.append("&remarks=" + map.get("remarks"));
				bf.append("&mark=" + map.get("mark"));
			}
		} else if (type.equals("15173")) {
			if (flag.equals("req")) {
				bf.append("bargainor_id=" + map.get("bargainor_id"));
				bf.append("&sp_billno=" + map.get("sp_billno"));
				bf.append("&pay_type=" + map.get("pay_type"));
				bf.append("&return_url=" + map.get("return_url"));
				bf.append("&attach=" + map.get("attach"));
			} else {
				bf.append("pay_result=" + map.get("pay_result"));
				bf.append("&bargainor_id=" + map.get("bargainor_id"));
				bf.append("&sp_billno=" + map.get("sp_billno"));
				bf.append("&total_fee=" + map.get("total_fee"));
				bf.append("&attach=" + map.get("attach"));
			}
		} else if (type.equals("70Pay")) {
			if (flag.equals("req1")) {
				bf.append("userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&bankid=").append(map.get("bankid"));
			} else if (flag.equals("req2")) {
				bf.append("money=").append(map.get("money"));
				bf.append("&userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&bankid=").append(map.get("bankid"));
			} else if (flag.equals("notify1")) {
				bf.append("returncode=").append(map.get("returncode"));
				bf.append("&userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
			} else {
				bf.append("money=").append(map.get("money"));
				bf.append("&returncode=").append(map.get("returncode"));
				bf.append("&userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
			}
		} else if (type.equals("STPay")) {
			if (flag.equals("bank_req")) {
				bf.append("parter=").append(map.get("parter"));
				bf.append("&type=").append(map.get("type"));
				bf.append("&value=").append(map.get("value"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&callbackurl=").append(map.get("callbackurl"));
			}
			if (flag.equals("card_req")) {
				bf.append("type=" + map.get("type"));
				bf.append("&parter=" + map.get("parter"));
				bf.append("&cardno=" + map.get("cardno"));
				bf.append("&cardpwd=" + map.get("cardpwd"));
				bf.append("&value=" + map.get("value"));
				bf.append("&restrict=" + map.get("restrict"));
				bf.append("&orderid=" + map.get("orderid"));
				bf.append("&callbackurl=" + map.get("callbackurl"));
			}
			if (flag.equals("bank_notify")) {
				bf.append("orderid=" + map.get("orderid"));
				bf.append("&opstate=" + map.get("opstate"));
				bf.append("&ovalue=" + map.get("ovalue"));
			}
		} else if (type.equals("koubei")) {
			if (flag.equals("req")) {
				bf.append("userid=").append(map.get("userid"));
				bf.append("&gateid=").append(map.get("gateid"));
				bf.append("&ordermoney=").append(map.get("ordermoney"));
				bf.append("&billno=").append(map.get("billno"));
				bf.append("&keyvalue=").append(map.get("keyvalue"));
				bf.append("&returnurl=").append(map.get("returnurl"));
				bf.append("&notifyurl=").append(map.get("notifyurl"));
				bf.append("&extdata=").append(map.get("extdata"));
				bf.append("&ver=").append(map.get("ver"));
			} else if (flag.equals("notify")) {
				bf.append("result=").append(map.get("result"));
				bf.append("&userid=").append(map.get("userid"));
				bf.append("&gateid=").append(map.get("gateid"));
				bf.append("&ordermoney=").append(map.get("ordermoney"));
				bf.append("&billno=").append(map.get("billno"));
				bf.append("&keyvalue=").append(map.get("keyvalue"));
				bf.append("&extdata=").append(map.get("extdata"));
				bf.append("&ordersid=").append(map.get("ordersid"));
			}
		} else if (type.equals("60866")) {
			if (flag.equals("req1")) {
				bf.append("userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&bankid=").append(map.get("bankid"));
			} else if (flag.equals("req2")) {
				bf.append("userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&bankid=").append(map.get("bankid"));
				bf.append("&money=").append(map.get("money"));
			} else if (flag.equals("notify")) {
				bf.append("returncode=").append(map.get("returncode"));
				bf.append("&userid=").append(map.get("userid"));
				bf.append("&orderid=").append(map.get("orderid"));
			}
		} else if (type.equals("ilongpay")) {
			if (flag.equals("req")) {
				bf.append("userid=").append(map.get("userid"));
				bf.append("&agentid=").append(map.get("agentid"));
				bf.append("&suppid=").append(map.get("suppid"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&orderAmt=").append(map.get("orderAmt"));
				bf.append("&orderdetail=").append(map.get("orderdetail"));
				bf.append("&callbackurl=").append(map.get("callbackurl"));
			} else if (flag.equals("req1")) {
				bf.append("parter=").append(map.get("parter"));
				bf.append("&type=").append(map.get("type"));
				bf.append("&value=").append(map.get("value"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&callbackurl=").append(map.get("callbackurl"));
			} else if (flag.equals("req2")) {
				bf.append("type=").append(map.get("type"));
				bf.append("&parter=").append(map.get("parter"));
				bf.append("&cardno=").append(map.get("cardno"));
				bf.append("&cardpwd=").append(map.get("cardpwd"));
				bf.append("&value=").append(map.get("value"));
				bf.append("&restrict=").append(map.get("restrict"));
				bf.append("&orderid=").append(map.get("orderid"));
				bf.append("&callbackurl=").append(map.get("callbackurl"));
			} else {
				bf.append("orderid=").append(map.get("orderid"));
				bf.append("&opstate=").append(map.get("opstate"));
				bf.append("&ovalue=").append(map.get("ovalue"));
			}
		} else if (type.equals("wanmei")) {
			if (flag.equals("req")) {
				bf.append(map.get("p0_Cmd"));
				bf.append(map.get("p1_MerId"));
				bf.append(map.get("p2_Order"));
				bf.append(map.get("p3_Cur"));
				bf.append(map.get("p4_Amt"));
				bf.append(map.get("p7_Pdesc"));
				bf.append(map.get("p8_Url"));
				bf.append(map.get("p9_MP"));
				bf.append(map.get("pa_FrpId"));
				if (map.containsKey("pg_BankCode")) {
					bf.append(map.get("pg_BankCode"));
				}
			}
		} else if (type.equals("186ue")) {
			if (flag.equals("req1")) {
				bf.append(map.get("p0_Cmd"));
				bf.append(map.get("p1_MerId"));
				bf.append(map.get("p2_Order"));
				bf.append(map.get("p3_Amt"));
				bf.append(map.get("p4_Cur"));
				bf.append(map.get("p8_Url"));
				bf.append(map.get("pa_MP"));
				bf.append(map.get("pd_FrpId"));
				bf.append(map.get("pr_NeedResponse"));
			} else if (flag.equals("req2")) {
				bf.append(map.get("p0_Cmd"));
				bf.append(map.get("p1_MerId"));
				bf.append(map.get("p2_Order"));
				bf.append(map.get("p3_Amt"));
				bf.append(map.get("p4_verifyAmt"));
				bf.append(map.get("p8_Url"));
				bf.append(map.get("pa_MP"));
				bf.append(map.get("pa7_cardAmt"));
				bf.append(map.get("pa8_cardNo"));
				bf.append(map.get("pa9_cardPwd"));
				bf.append(map.get("pd_FrpId"));
				bf.append(map.get("pr_NeedResponse"));
			} else if (flag.equals("notify1")) {
				bf.append(map.get("p1_MerId"));
				bf.append(map.get("r0_Cmd"));
				bf.append(map.get("r1_Code"));
				bf.append(map.get("r2_TrxId"));
				bf.append(map.get("r3_Amt"));
				bf.append(map.get("r4_Cur"));
				bf.append(map.get("r5_Pid"));
				bf.append(map.get("r6_Order"));
				bf.append(map.get("r7_Uid"));
				bf.append(map.get("r8_MP"));
				bf.append(map.get("r9_BType"));
			} else {
				bf.append(map.get("r0_Cmd"));
				bf.append(map.get("r1_Code"));
				bf.append(map.get("p1_MerId"));
				bf.append(map.get("p2_Order"));
				bf.append(map.get("p3_Amt"));
				bf.append(map.get("p4_FrpId"));
				bf.append(map.get("p5_CardNo"));
				bf.append(map.get("p6_confirmAmount"));
				bf.append(map.get("p7_realAmount"));
				bf.append(map.get("p8_cardStatus"));
				bf.append(map.get("p9_MP"));
				bf.append(map.get("pb_BalanceAmt"));
				bf.append(map.get("pc_BalanceAct"));
			}
		}else if (type.equals("shuangqian")){
			if (flag.equals("req")){
				bf.append("Amount="+map.get("Amount"));
				bf.append("&BillNo="+map.get("BillNo"));
				bf.append("&MerNo="+map.get("MerNo"));
				bf.append("&PayType="+map.get("PayType"));
			}else if(flag.equals("notify")){
				bf.append("Amount="+map.get("Amount"));
				bf.append("&BillNo="+map.get("BillNo"));
				bf.append("&MerNo="+map.get("MerNo"));
				bf.append("&Succeed="+map.get("Succeed"));
			}
		}
		return bf.toString();
	}

	/**
	 * 截取二维码字符串
	 */
	public String urlToErCode(String type, String url) {
		String scan_url = "-1";
		if (type.equals("dbt")) {
			String[] arrays = url.split("=");
			scan_url = arrays[1];
		}
		return scan_url;
	}

	public void encoderQRCoder(String content, HttpServletResponse response) {
		try {
			Qrcode qrcodeHandler = new Qrcode();

			// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
			qrcodeHandler.setQrcodeErrorCorrect('M');

			// 设置编码模式
			qrcodeHandler.setQrcodeEncodeMode('B');

			// 1 — 40 共40个版本，1 21x21模块、40 177x177模块，每增加一个版本每边增加4个模块，如: 版本2
			// 为25x25模块
			
			byte[] contentBytes = content.getBytes("UTF-8");
			int size = 250;
			if(contentBytes.length < 122){
				qrcodeHandler.setQrcodeVersion(7);// 版本7最多可存储122位字符，40个中文汉字
			}else{
				qrcodeHandler.setQrcodeVersion(15);
				size = 67+24*(15-1);
			}
			
			BufferedImage bufImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			// 设置背景色 白色
			gs.setBackground(Color.WHITE);
			gs.clearRect(0, 0,size, size);
			// 设定图像颜色 黑色
			gs.setColor(Color.BLACK);
			// 设置偏移量 不设置可能导致解析出错
			int pixoff = 13;
			// 输出内容> 二维码
			if (contentBytes.length > 0 && contentBytes.length < 800) {
				boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);
				for (int i = 0; i < codeOut.length; i++) {
					for (int j = 0; j < codeOut.length; j++) {
						if (codeOut[j][i]) {
							gs.fillRect(j * 5 + pixoff, i * 5 + pixoff, 5, 5);
						}
					}
				}
			} else {
				System.err.println("QRCode content bytes length = " + contentBytes.length + " not in [ 0,120 ]. ");
			}
			gs.dispose();
			bufImg.flush();
			// 生成二维码QRCode图片
			response.setContentType("image/png");
			ImageIO.write(bufImg, "png", response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public <v> String getInputForm(Map<String, v> map) {
		StringBuffer sbHtml = new StringBuffer();
		String codeImage = "0";
		String reqUrl = "ScanCode";
		if (map.containsKey("codeImage")){
			codeImage = String.valueOf(map.get("codeImage"));
		}
		if (map.containsKey("riskUri")){
			reqUrl = String.valueOf(map.get("riskUri"))+"/ScanCode";
		}
		sbHtml.append(
				"<html> <head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
		sbHtml.append("<form id=\"scanCodeSubmit\" action=\""+reqUrl+"\" method=\"post\">");
		sbHtml.append("<input type=\"hidden\" name=\"type\" value=\"" + map.get("type") + "\"/>");
		sbHtml.append("<input type=\"hidden\" name=\"qrCodeUrl\" value=\"" + map.get("qrCodeUrl") + "\"/>");
		sbHtml.append("<input type=\"hidden\" name=\"total_fee\" value=\"" + map.get("total_fee") + "\"/>");
		sbHtml.append("<input type=\"hidden\" name=\"orderNumber\" value=\"" + map.get("orderNumber") + "\"/>");
		sbHtml.append("<input type=\"hidden\" name=\"codeImage\" value=\"" + codeImage + "\"/>");
		// submit按钮控件请不要含有name属性
		sbHtml.append("</form>");
		sbHtml.append("<script>document.forms['scanCodeSubmit'].submit();</script>");
		sbHtml.append("</body></html>");
		return sbHtml.toString();
	}

	/**
	 * 获取post过来的参数
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getParams(HttpServletRequest request) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = iter.next().toString();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
			params.put(name, valueStr);
		}
		return params;
	}
	
	public String getXmlParams(HttpServletRequest request){
        String body = "";
        try {
            ServletInputStream inputStream = request.getInputStream(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while(true){
                String info = br.readLine();
                if(info == null){
                    break;
                }
                if(body == null || "".equals(body)){
                    body = info;
                }else{
                    body += info;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }            
        return body;
    }
	
}
