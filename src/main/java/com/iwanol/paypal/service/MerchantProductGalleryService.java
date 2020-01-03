package com.iwanol.paypal.service;

import com.iwanol.paypal.domain.MerchantProductGallery;

public interface MerchantProductGalleryService {
	MerchantProductGallery findGalleryIdByMerchantIdAndProductId(Long merchantId,Long productId);
}
