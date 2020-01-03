package com.iwanol.paypal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.repository.GalleryRepository;
import com.iwanol.paypal.service.GalleryService;

@Service
public class GalleryServiceImpl extends BaseServiceImpl<Gallery> implements GalleryService{
	@Autowired
	private GalleryRepository galleryRepository;
	@Override
	public JpaRepository<Gallery, Long> getRepository() {
		// TODO Auto-generated method stub
		return galleryRepository;
	}

}
