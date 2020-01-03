package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.AccountDetails;
import com.iwanol.paypal.repository.AccountDetailsRepository;
import com.iwanol.paypal.service.AccountDetailsService;

@Service
public class AccountDetailsServiceImpl extends BaseServiceImpl<AccountDetails> implements AccountDetailsService{
	@Autowired
	private AccountDetailsRepository accountDetailsRepository;
	@Override
	public JpaRepository<AccountDetails, Long> getRepository() {
		// TODO Auto-generated method stub
		return accountDetailsRepository;
	}

}
