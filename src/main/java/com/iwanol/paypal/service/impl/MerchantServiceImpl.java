package com.iwanol.paypal.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.repository.MerchantRepository;
import com.iwanol.paypal.service.MerchantService;
@Service
public class MerchantServiceImpl extends BaseServiceImpl<Merchant> implements MerchantService{
	@Autowired
	private MerchantRepository merchantRepository;
	@Override
	public Map<String, Object> findMerchantState(String account) {
		// TODO Auto-generated method stub
		return merchantRepository.findMerchantState(account);
	}

	@Override
	public JpaRepository<Merchant, Long> getRepository() {
		// TODO Auto-generated method stub
		return merchantRepository;
	}

	@Override
	public Merchant findByAccount(String account) {
		// TODO Auto-generated method stub
		return merchantRepository.findByAccount(account);
	}

	@Override
	public Map<String, String> findKeyByAccount(String account) {
		// TODO Auto-generated method stub
		return merchantRepository.findKeyByAccount(account);
	}

}
