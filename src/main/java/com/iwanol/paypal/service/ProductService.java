package com.iwanol.paypal.service;

import com.iwanol.paypal.domain.Product;

public interface ProductService {
	Product findByProductMark(String mark);
}
