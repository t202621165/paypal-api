package com.iwanol.paypal.payee.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.payee.PayeeInterface;
import com.iwanol.paypal.payee.daifu.DaifuFactory;
import com.iwanol.paypal.repository.MerchantRepository;
import com.iwanol.paypal.service.PayeeService;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;

@Component
public class DoPayee implements PayeeInterface{
	@Autowired
	private PayeeService payeeService;
	@Autowired
	private DaifuFactory daifuFactory;
	@Autowired
	private MerchantRepository merchantRepository;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public JSONObject init(Merchant merchant, PayeeVo v) {
		// TODO Auto-generated method stub
		JSONObject valid = this.valid(merchant,v.getTotal_amount());
		if (valid.getBooleanValue("state")){
			Payee payee = this.getPayee();
			if (payee != null){
				v.setCost(String.valueOf(merchant.getFee()));
				v.setmId(String.valueOf(merchant.getId()));
				JSONObject result = this.execute(payee, v);
				return result;
			}
			return Result.error.toJson("代付服务关闭");
		}
		return valid;
	}

	@Override
	public JSONObject valid(Merchant merchant,String amount) {
		// TODO Auto-generated method stub
		Bank bank = merchant.getBanks().stream().filter(b -> b.getBankType().equals(true)).findFirst().get();
		BigDecimal money = bank.getOverMoney().subtract(new BigDecimal(amount)).subtract(merchant.getFee());
		
		if (merchant.getState() != 1){
			return Result.error.toJson("商户状态异常,拒绝代付");
		}
		
		if (!bank.getPayeeState()){
			return Result.error.toJson("未开启代付功能");
		}
		
		if (money.compareTo(BigDecimal.ZERO) < 0){
			return Result.error.toJson("账户余额不足");
		}
		return Result.success.toJson("校验成功");
	}

	@Override
	public Payee getPayee() {
		// TODO Auto-generated method stub
	    Optional<Payee> optional = payeeService.findByIsDefault(true);
	    if (optional.isPresent()){
	    	return optional.get();
	    }
		return null;
	}

	@Override
	public JSONObject execute(Payee payee, PayeeVo v) {
		// TODO Auto-generated method stub
		logger.info("代付标识:{}",payee.getMark());
		v.setDiscription("奇易代付");
		try {
			String mark = payee.getMark();
			if (mark.contains("_")){
				mark = mark.split("\\_")[0]; 
			}
			return daifuFactory.createDaiFuPayee(payee.getMark()).init(payee, v);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Result.error.toJson(e.getMessage());
		}
	}

	public void txSms(String merId,String amount){
		try {
			String tel = merchantRepository.findById(1L).get().getTelPhone();
			Integer appId = 1400026790;
			String appKey = "413ef0456665971b62fb82479849715d";
			Integer tempId = 13464;
			ArrayList<String> lists = new ArrayList<String>();
			// 初始化腾讯云单发模版
			SmsSingleSender singleSender = new SmsSingleSender(appId, appKey);
			lists.add(merId);
			lists.add(amount);
			SmsSingleSenderResult singleSenderResult = singleSender.sendWithParam("86", tel,
					tempId, lists, "", "", "");
			if (singleSenderResult.result == 0) { // 成功
				logger.info("提现通知短信发送成功");
			} else {
				logger.info("提现通知短信发送失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
