package com.iwanol.paypal.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.SystemSet;
import com.iwanol.paypal.service.impl.SystemSetServiceImpl;
import com.iwanol.paypal.vo.Message;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class WebController {
	@Autowired
	private SystemSetServiceImpl systemSetServiceImpl;
	/**
	 * 403 服务器积极拒绝返回页面
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@GetMapping("/403")
	public String welcome(ModelMap map,Model model) throws UnsupportedEncodingException{
		if (!StringUtils.isEmpty(map.get("message"))){
			model.addAttribute("message", JSONObject.parseObject(map.get("message").toString()));
		}else{
			model.addAttribute("message",JSONObject.parseObject(Message.result.ret("10031")));
		}
		return "error/403";
	}
	
	@GetMapping("/except")
	public String error(HttpServletRequest request,RedirectAttributes attr){
		if(!StringUtils.isEmpty(request.getAttribute("message"))){
			JSONObject message = JSONObject.parseObject(request.getAttribute("message").toString());
			attr.addFlashAttribute("message",message);
		}
		return "redirect:/403";
	}
	
	/**
	 *404
	 * @return
	 */
	@GetMapping("/404")
	public String unfind(){
		return "error/404";
	}
	
	@GetMapping("")
	public String root(RedirectAttributes attr) throws UnsupportedEncodingException{
		attr.addFlashAttribute("message", JSONObject.parseObject(Message.result.ret("10031")));
		return "redirect:/403";
	}
	
	/**
	 * 跳转二维码扫码页面
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@PostMapping("/ScanCode")
	public ModelAndView ScanCode(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		String type = request.getParameter("type");
		String qrCodeUrl = request.getParameter("qrCodeUrl");
		String total_fee = request.getParameter("total_fee");
		String orderNumber = request.getParameter("orderNumber");
		SystemSet systemSet = systemSetServiceImpl.findEntitys().get(0);
		String codeImage = request.getParameter("codeImage");
		ModelAndView mv = null;
		try {
			if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(qrCodeUrl) && !StringUtils.isEmpty(total_fee) && !StringUtils.isEmpty(orderNumber)) {
				String title = "";
				String body = "";
				String flag = "";
				Map<String,Object> jsonMap = new HashMap<String,Object>();
				if ("alipay".equals(type)) {
					flag = "1";
					title = "支付宝扫码";
					body = "支付宝";
				}else if ("wechat".equals(type)) {
					flag = "1";
					title = "微信扫码";
					body = "微信";
				}else if ("qpay".equals(type)) {
					flag = "1";
					title = "QQ钱包扫码";
					body = "QQ";
				}else if ("hbpay".equals(type)) {
					flag = "1";
					title = "蚂蚁花呗";
					body = "支付宝";
				}else if ("alipay_gf".equals(type)) {
					flag = "1";
					title = "支付宝扫码";
					body = "支付宝";
				}else if ("ecode".equals(type)) {
					flag = "1";
					title = "银联扫码";
					body = "银行";
				}else if ("meituan".equals(type)) {
					  flag = "1";
			          title = "美团扫码";
			          body = "美团";
			    }else if ("jingdong".equals(type)) {
			    	  flag = "1";
			          title = "京东扫码";
			          body = "京东";
			    }else if ("dzdp".equals(type)) {
			    	  flag = "1";
			          title = "大众点评";
			          body = "大众点评";
			    }else if ("cft".equals(type)){
			    	flag = "1";
					title = "财付通扫码";
					body = "财付通";
			    }else if ("ebank".equals(type)){
			    	flag = "1";
					title = "网银扫码";
					body = "银行";
			    }
				
				jsonMap.put("codeImage", codeImage);
				jsonMap.put("type", type);
				jsonMap.put("flag", flag);
				jsonMap.put("body", body);
				jsonMap.put("title", title);
				jsonMap.put("qrCodeUrl", qrCodeUrl);
				jsonMap.put("total_fee", total_fee);
				jsonMap.put("orderNumber", orderNumber);
				jsonMap.put("servicePhone", systemSet.getServicePhone().isEmpty() ? "--" : systemSet.getServicePhone());
				jsonMap.put("serviceQQ", systemSet.getServiceQQ().isEmpty() ? "--" : systemSet.getServiceQQ());
				mv = new ModelAndView("scan/ScanCode","jsonMap",jsonMap);		
			}else{
				request.setAttribute("message",JSONObject.parseObject(Message.result.ret("10031")));
				request.getRequestDispatcher("/except").forward(request, response);			
			}
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("message",JSONObject.parseObject(Message.result.ret("10012")));
			request.getRequestDispatcher("/except").forward(request, response);		
		}
		return mv;
	}
	
}
