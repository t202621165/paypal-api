package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.ProductRate;

public interface ProductRateRepository extends JpaRepository<ProductRate,Long> {
	ProductRate findByMerchantIdAndProductId(Long merchantId,Long productId);
}
