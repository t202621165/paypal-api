package com.iwanol.paypal.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.SystemSet;
import com.iwanol.paypal.repository.SystemSetRepository;
import com.iwanol.paypal.service.SystemSetService;
@Service
public class SystemSetServiceImpl extends BaseServiceImpl<SystemSet> implements SystemSetService{
	@Autowired
	private SystemSetRepository systemSetRepository;
	@Override
	public JpaRepository<SystemSet, Long> getRepository() {
		// TODO Auto-generated method stub
		return systemSetRepository;
	}
	@Override
	public BigDecimal findTailScope(String mark) {
		// TODO Auto-generated method stub
		return systemSetRepository.findTailScope(mark);
	}

}
