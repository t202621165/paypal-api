package com.iwanol.paypal.service;

import java.util.Map;

public interface RouteService {
	Map<String,String> findReqUrl(Long galleryId,Long productId);
}
