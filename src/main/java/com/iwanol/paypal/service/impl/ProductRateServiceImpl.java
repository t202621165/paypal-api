package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.ProductRate;
import com.iwanol.paypal.repository.ProductRateRepository;
import com.iwanol.paypal.service.ProductRateService;
@Service
public class ProductRateServiceImpl extends BaseServiceImpl<ProductRate> implements ProductRateService{
	@Autowired
	private ProductRateRepository productRateRepository;
	@Override
	public ProductRate findByMerchantIdAndProductId(Long merchantId, Long productId) {
		// TODO Auto-generated method stub
		return productRateRepository.findByMerchantIdAndProductId(merchantId, productId);
	}

	@Override
	public JpaRepository<ProductRate, Long> getRepository() {
		// TODO Auto-generated method stub
		return productRateRepository;
	}

}
