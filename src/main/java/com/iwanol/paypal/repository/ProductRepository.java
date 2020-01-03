package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {
	Product findByProductMark(String productMark);
}
