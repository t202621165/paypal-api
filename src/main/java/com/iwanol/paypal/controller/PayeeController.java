package com.iwanol.paypal.controller;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.payee.impl.DoPayee;
import com.iwanol.paypal.service.MerchantService;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;

@Controller
public class PayeeController {
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private DoPayee doPayee;
	
	private ReentrantLock lock = new ReentrantLock();
	
	/**
	 * 代付
	 * @param v
	 * @return
	 */
	@GetMapping("/payee")
	public @ResponseBody JSONObject payee(PayeeVo v){
		try{
			lock.lock();
			if (StringUtils.isEmpty(v.getMerchant_id()))
				return Result.error.toJson("商户Id不能为空");
			Merchant merchant = merchantService.findByAccount(v.getMerchant_id());
			JSONObject result = v.valid(merchant.getMerchantKey());
			if (result.getBooleanValue("state")){
				return doPayee.init(merchant, v);
			}
			return result;
		}catch (Exception e) {
			// TODO: handle exception
			return Result.error.toJson("代付异常:"+e.getMessage());
		}finally {
			lock.unlock();
		}
		
	}
	
	/**
	 * 商户提现
	 * @param v
	 * @return
	 */
	@GetMapping("/tx")
	public @ResponseBody JSONObject txAudit(PayeeVo v){
		try{
			lock.lock();
			v.setType("tx");
			if (StringUtils.isEmpty(v.getMerchant_id()))
				return Result.error.toJson("商户Id不能为空");
			Merchant merchant = merchantService.findByAccount(v.getMerchant_id());
			JSONObject result = v.valid(merchant.getMerchantKey());
			if (result.getBooleanValue("state")){
				//发送短信
				doPayee.txSms(String.valueOf(merchant.getId()),v.getTotal_amount());
				return doPayee.init(merchant, v);
			}
			return result;
		}catch (Exception e) {
			// TODO: handle exception
			return Result.error.toJson("代付异常:"+e.getMessage());
		}finally {
			lock.unlock();
		}
	}
	
	/**
	 * 提现支付
	 * @param v
	 * @return
	 */
	@GetMapping("/tx/payee")
	public @ResponseBody JSONObject txPayee(PayeeVo v){
		try{
			lock.lock();
			v.setType("txPayee");
			if (StringUtils.isEmpty(v.getMerchant_id()))
				return Result.error.toJson("商户Id不能为空");
			Merchant merchant = merchantService.findByAccount(v.getMerchant_id());
			JSONObject result = v.valid(merchant.getMerchantKey());
			if (result.getBooleanValue("state")){
				return doPayee.init(merchant, v);
			}
			return result;
		}catch (Exception e) {
			// TODO: handle exception
			return Result.error.toJson("代付异常:"+e.getMessage());
		}finally {
			lock.unlock();
		}
	}
}
