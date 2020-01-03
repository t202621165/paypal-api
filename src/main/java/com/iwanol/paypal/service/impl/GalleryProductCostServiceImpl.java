package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.GalleryProductCost;
import com.iwanol.paypal.repository.GalleryProductCostRepository;
import com.iwanol.paypal.service.GalleryProductCostService;
@Service
public class GalleryProductCostServiceImpl extends BaseServiceImpl<GalleryProductCost> implements GalleryProductCostService{
	@Autowired
	private GalleryProductCostRepository galleryProductCostRepository;
	@Override
	public GalleryProductCost findByGalleryIdAndProductId(Long galleryId, Long productId) {
		// TODO Auto-generated method stub
		return galleryProductCostRepository.findByGalleryIdAndProductId(galleryId, productId);
	}

	@Override
	public JpaRepository<GalleryProductCost, Long> getRepository() {
		// TODO Auto-generated method stub
		return galleryProductCostRepository;
	}

}
