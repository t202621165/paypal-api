package com.iwanol.paypal.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.repository.PayeeRepository;
import com.iwanol.paypal.service.PayeeService;

@Service
public class PayeeServiceImpl extends BaseServiceImpl<Payee> implements PayeeService{
	@Autowired
	private PayeeRepository payeeRepository;
	@Override
	public Optional<Payee> findByIsDefault(Boolean isDefault) {
		// TODO Auto-generated method stub
		return payeeRepository.findByIsDefault(isDefault);
	}

	@Override
	public JpaRepository<Payee, Long> getRepository() {
		// TODO Auto-generated method stub
		return payeeRepository;
	}

}
