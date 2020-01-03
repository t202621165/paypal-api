package com.iwanol.paypal.service.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.AccountDetails;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.repository.AccountDetailsRepository;
import com.iwanol.paypal.repository.BankRepository;
import com.iwanol.paypal.repository.OrderRepository;
import com.iwanol.paypal.service.OrderService;
@Service
public class OrderServiceImpl extends BaseServiceImpl<PlatformOrder> implements OrderService{
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private AccountDetailsRepository accountDetailsRepository;
	@Autowired
	private BankRepository bankRepository;
	
	@Override
	public Boolean findMerchantOrderIsExit(String merchantOrder) {
		// TODO Auto-generated method stub
		Boolean result = true;
		Map<String,Object> map = orderRepository.findOrderIsExit(merchantOrder);
		if(map.get("count").toString().equals("0")){
			result = false;
		}
		return result;
	}

	@Override
	public JpaRepository<PlatformOrder, Long> getRepository() {
		// TODO Auto-generated method stub
		return orderRepository;
	}

	@Override
	public PlatformOrder findByMerchantOrder(String merchantOrder) {
		// TODO Auto-generated method stub
		return orderRepository.findByMerchantOrderNumber(merchantOrder);
	}

	@Override
	public PlatformOrder findBySysOrder(String sysOrder) {
		// TODO Auto-generated method stub
		return orderRepository.findBySysOrderNumber(sysOrder);
	}

	@Override
	public Map<String, Integer> findStateBySysOrder(String sysOrder) {
		// TODO Auto-generated method stub
		return orderRepository.findStateBySysOrder(sysOrder);
	}

	@Override
	@Transactional
	public void opration(PlatformOrder platformOrder, Bank bank, AccountDetails accountDetails) {
		// TODO Auto-generated method stub
		orderRepository.save(platformOrder);//更新订单
		bank = bankRepository.save(bank);//更新余额
		accountDetails.setOver_money(bank.getOverMoney());
		accountDetailsRepository.save(accountDetails);//更新账户明细
	}

	@Override
	@Transactional
	public PlatformOrder findSelectivBySysOrder(String sysOrder) {
		// TODO Auto-generated method stub
		PlatformOrder platformOrder = orderRepository.findBySysOrderNumber(sysOrder);
		platformOrder.setProductName(platformOrder.getProduct().getProductName());
		platformOrder.setProductMark(platformOrder.getProduct().getProductMark());
		platformOrder.setMerchantAccount(platformOrder.getMerchant().getAccount());
		platformOrder.setMerchantKey(platformOrder.getMerchant().getMerchantKey());
		if(StringUtils.isEmpty(platformOrder.getMerchant().getRetUrl())){
			platformOrder.setRetUrl(platformOrder.getRetUrl());
		}else{
			platformOrder.setRetUrl(platformOrder.getMerchant().getRetUrl());
		}
		return platformOrder;
	}

	@Override
	public PlatformOrder findByPartyOrderNumber(String partyOrderNumber) {
		// TODO Auto-generated method stub
		return orderRepository.findByPartyOrderNumber(partyOrderNumber);
	}

	@Override
	public void updateClientIp(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String sysOrderNumber = request.getParameter("orderNumber");
		String ip = request.getParameter("ip");
		orderRepository.updateClientIp(ip,sysOrderNumber);
	}

	@Override
	public Map<String,Object> findOrderByMerchantOrderNumber(String merchantOrderNumber) {
		// TODO Auto-generated method stub
		return orderRepository.orderQuery(merchantOrderNumber);
	}

}
