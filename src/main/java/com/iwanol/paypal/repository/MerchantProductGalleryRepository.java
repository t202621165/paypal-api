package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.MerchantProductGallery;

public interface MerchantProductGalleryRepository extends JpaRepository<MerchantProductGallery,Long> {
	MerchantProductGallery findByMerchantIdAndProductId(Long merchantId,Long productId);
}
