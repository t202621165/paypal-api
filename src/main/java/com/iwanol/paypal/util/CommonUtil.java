package com.iwanol.paypal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

/**
 * 公共工具类
 * 
 * @author leo
 *
 */
public class CommonUtil {
	public final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
	public final String DATE = "yyyy-MM-dd";
	public final String YMDHFS = "yyyyMMddHHmmss";
	private static CommonUtil commonUtil;
	public static CommonUtil getCommonUtil(){
		if(commonUtil == null){
			commonUtil = new CommonUtil();
			return commonUtil;
		}else{
			return commonUtil;
		}
	}
	// 生成随机数字和字母,
	public String getStringRandom(int length) {
		StringBuffer buf = new StringBuffer();
		Random random = new Random();
		// 参数length，表示生成几位随机数
		for (int i = 0; i < length; i++) {

			int number = random.nextInt(2);
			// 输出字母还是数字
			if (number % 2 == 0) {
				// 输出是大写字母还是小写字母
				int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
				buf.append((char) (random.nextInt(26) + temp));
			} else {
				buf.append(String.valueOf(random.nextInt(10)));
			}
		}
		return buf.toString();
	}

	/**
	 * 保存token到浏览器cookie
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public void saveTokenToCookie(HttpServletResponse response, String token)
			throws UnsupportedEncodingException {
		String value = URLEncoder.encode(token, "utf-8");
		Cookie cookie = new Cookie("token", value);
		cookie.setMaxAge(3600 * 24 * 5);// 设置其生命周期
		response.addCookie(cookie);
	}

	/**
	 * 获取cookie中的cookie
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getTokenCookie(HttpServletRequest request) throws UnsupportedEncodingException {
		String token = "";
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("token")) {
					token = cookie.getValue();
				}
			}
		}
		return URLDecoder.decode(token, "utf-8");
	}

	/***
	 * 获取浏览器IP
	 * 
	 * @param request
	 * @return
	 */
	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("http_client_ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		// 如果是多级代理，那么取第一个ip为客户ip
		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
		}
		return ip;
	}
	
	/**
	 * 生成商户账号
	 * @return
	 */
	public String createAccount(int len){
		StringBuffer buf = new StringBuffer();
		SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmm");
		Random random= new Random();
		for(int i = 0; i<len; i++){
			buf.append(random.nextInt(10));
		}
		String account = format.format(new Date())+buf.toString();
		return account;
	}
	
	/**
	 * 生成随机数
	 * @param len
	 * @return
	 */
	public String random(int len){
		StringBuffer buf = new StringBuffer();
		Random random = new Random();
		for(int i = 0; i<len; i++){
			buf.append(random.nextInt(10));
		}
		return buf.toString();
	}
	
	/**
	 * 字符串转Date
	 * @param datetime
	 * @return
	 * @throws ParseException
	 */
	public Date stringToDate(String format,String datetime){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try{
			Date date = sdf.parse(datetime);
			return date;
		}catch(Exception e){
			
		}
		return null;
	}
	
	/**
	 * 获取当前时间
	 * @return
	 */
	public String currentDateTime(String format,Date date){
		SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
		if(date == null){
			return df.format(new Date());
		}else{
			return df.format(date);
		}
	}
	
	public String createOrder(String flag,Integer len, String serverFlag){
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
		String number = flag+sdf.format(new Date())+random(len)+serverFlag;
		return number;
	}
	
	/**
     * endDate比startDate多的天数
     * @param startDate    
     * @param endDate
     */
	public int differentDays(String startDate, String endDate) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 定义返回日期差
		int returnDay = 0;
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(format.parse(startDate));

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(format.parse(endDate));
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if (year1 != year2){// 同一年
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0){// 闰年
					timeDistance += 366;
				} else{// 不是闰年
					timeDistance += 365;
				}
			}
			returnDay = timeDistance + (day2 - day1);
		}else{// 不同年
			returnDay = day2 - day1;
		}
		return returnDay;
	}
	
	/**
	 * 返回昨日 年-月-日-时-分
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String nextDateTime(Date date,int row){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); 
		calendar.add(calendar.DATE,row);
		String dates = format.format(calendar.getTime());
		return dates;
	}
	
	public String beforeCurrentDate(String format,Integer beforeDay){
		DateFormat df = new SimpleDateFormat(format);
		String time = df.format(new Date().getTime()-beforeDay*24*60*60*1000);
		return time;
	}
	
	/**
	 * 获取服务器标识
	 */
	@SuppressWarnings("static-access")
	public String getServerFlag(){
		InetAddress addr;
		String flag = "0";
		try{
			addr = InetAddress.getLocalHost();
			String[] ips = addr.getLocalHost().toString().split(".");
			flag = ips[3];
		}catch(Exception e){
			
		}
		return flag;
	}
	
	public BigDecimal tailAmount(String scope) {
		Random rand = new Random();
		float f = rand.nextFloat();
		BigDecimal tailAmount = BigDecimal.valueOf(f).setScale(2, RoundingMode.FLOOR);
		if (tailAmount.compareTo(new BigDecimal(scope)) < 0) {
			tailAmount = tailAmount(scope);
		}
		return tailAmount;
	}
	
	public String getCurrentDomain(HttpServletRequest request){
		String uri = ""; // 获取当前域名
		Integer port = request.getServerPort(); // 获取当前端口
		String scheme = request.getScheme(); // 获取通信协议
		if (port == 80) {
			uri = scheme + "://" + request.getServerName();
		} else {
			uri = scheme + "://" + request.getServerName() + ":" + port;
		}
		return uri;
	}
	
	public Map<String, Object> getUrlParams(String param) {
		Map<String, Object> map = new HashMap<String, Object>(0);
		if (StringUtils.isEmpty(param)) {
			return map;
		}
		String[] params = param.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) {
				map.put(p[0], p[1]);
			}
		}
		return map;
	}
	
	public String getInputStreamParam(HttpServletRequest request){
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
