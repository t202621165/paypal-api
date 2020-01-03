package com.iwanol.paypal.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.iwanol.paypal.domain.AccountDetails;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.domain.PlatformOrder;

public interface OrderService {
	Boolean findMerchantOrderIsExit(String merchantOrder);
	PlatformOrder findByMerchantOrder(String merchantOrder);
	PlatformOrder findBySysOrder(String sysOrder);
	PlatformOrder findByPartyOrderNumber(String partyOrderNumber);
	PlatformOrder findSelectivBySysOrder(String sysOrder);
	Map<String,Object> findOrderByMerchantOrderNumber(String merchantOrderNumber);
	Map<String,Integer> findStateBySysOrder(String sysOrder);
	void opration(PlatformOrder platformOrder,Bank bank,AccountDetails accountDetails);
	void updateClientIp(HttpServletRequest request);
}
