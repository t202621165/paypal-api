package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.MerchantProductGallery;
import com.iwanol.paypal.repository.MerchantProductGalleryRepository;
import com.iwanol.paypal.service.MerchantProductGalleryService;
@Service
public class MerchantProductGalleryServiceImpl extends BaseServiceImpl<MerchantProductGallery> implements MerchantProductGalleryService{
	@Autowired
	private MerchantProductGalleryRepository merchantProductGalleryRepository;
	@Override
	public MerchantProductGallery findGalleryIdByMerchantIdAndProductId(Long merchantId, Long productId) {
		// TODO Auto-generated method stub
		return merchantProductGalleryRepository.findByMerchantIdAndProductId(merchantId, productId);
	}

	@Override
	public JpaRepository<MerchantProductGallery, Long> getRepository() {
		// TODO Auto-generated method stub
		return merchantProductGalleryRepository;
	}

}
