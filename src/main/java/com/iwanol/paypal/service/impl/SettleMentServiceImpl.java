package com.iwanol.paypal.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.SettleMent;
import com.iwanol.paypal.repository.SettleMentRepository;
import com.iwanol.paypal.service.SettleMentService;

@Service
public class SettleMentServiceImpl extends BaseServiceImpl<SettleMent> implements SettleMentService{
	@Autowired
	private SettleMentRepository settleMentRepository;
	@Override
	public int updateStateAndDescBySerialNumber(String serialNumber, String description, Date commplate) {
		// TODO Auto-generated method stub
		return settleMentRepository.updateStateAndDescBySerialNumber(serialNumber, description, commplate);
	}

	@Override
	public JpaRepository<SettleMent, Long> getRepository() {
		// TODO Auto-generated method stub
		return settleMentRepository;
	}

}
