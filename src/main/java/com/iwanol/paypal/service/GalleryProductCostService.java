package com.iwanol.paypal.service;

import com.iwanol.paypal.domain.GalleryProductCost;

public interface GalleryProductCostService {
	GalleryProductCost findByGalleryIdAndProductId(Long galleryId,Long productId);
}
