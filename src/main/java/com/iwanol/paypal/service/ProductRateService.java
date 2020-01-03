package com.iwanol.paypal.service;

import com.iwanol.paypal.domain.ProductRate;

public interface ProductRateService {
	ProductRate findByMerchantIdAndProductId(Long merchantId,Long productId);
}
