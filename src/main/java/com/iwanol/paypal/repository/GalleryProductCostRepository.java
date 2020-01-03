package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.GalleryProductCost;

public interface GalleryProductCostRepository extends JpaRepository<GalleryProductCost,Long>{
	GalleryProductCost findByGalleryIdAndProductId(Long galleryId,Long productId);
}
