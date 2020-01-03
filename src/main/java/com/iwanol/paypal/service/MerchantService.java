package com.iwanol.paypal.service;

import java.util.Map;

import com.iwanol.paypal.domain.Merchant;

public interface MerchantService {
	Map<String,Object> findMerchantState(String account);
	
	Merchant findByAccount(String account);
	
	Map<String,String> findKeyByAccount(String account);
}
