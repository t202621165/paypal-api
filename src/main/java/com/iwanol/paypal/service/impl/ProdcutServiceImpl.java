package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Product;
import com.iwanol.paypal.repository.ProductRepository;
import com.iwanol.paypal.service.ProductService;
@Service
public class ProdcutServiceImpl extends BaseServiceImpl<Product> implements ProductService{
	@Autowired
	private ProductRepository productRepository;
	@Override
	public JpaRepository<Product, Long> getRepository() {
		// TODO Auto-generated method stub
		return productRepository;
	}
	@Override
	public Product findByProductMark(String mark) {
		// TODO Auto-generated method stub
		return productRepository.findByProductMark(mark);
	}

}
