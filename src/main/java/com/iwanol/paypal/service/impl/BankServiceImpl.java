package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Bank;
import com.iwanol.paypal.repository.BankRepository;
import com.iwanol.paypal.service.BankService;
@Service
public class BankServiceImpl extends BaseServiceImpl<Bank> implements BankService{
	@Autowired
	private BankRepository bankRepository;
	@Override
	public JpaRepository<Bank, Long> getRepository() {
		// TODO Auto-generated method stub
		return bankRepository;
	}
	@Override
	public Bank findByMerchantIdAndBankType(Long merchantId, Boolean bankType) {
		// TODO Auto-generated method stub
		return bankRepository.findByMerchantIdAndBankType(merchantId, bankType);
	}

}
