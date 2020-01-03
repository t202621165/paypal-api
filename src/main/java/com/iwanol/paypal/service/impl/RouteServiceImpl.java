package com.iwanol.paypal.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.impl.BaseServiceImpl;
import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.Route;
import com.iwanol.paypal.repository.RouteRepository;
import com.iwanol.paypal.route.RoundRobinScheduling;
import com.iwanol.paypal.service.RouteService;
@Service
public class RouteServiceImpl extends BaseServiceImpl<Route> implements RouteService{
	@Autowired
	private RouteRepository routeRepository;
	
	private Map<String,RoundRobinScheduling> RoundRobinSchedulings = new ConcurrentHashMap<String,RoundRobinScheduling>();
	
	public RouteServiceImpl(List<RoundRobinScheduling> list){
		this.RoundRobinSchedulings = list.parallelStream().collect(Collectors.toMap(RoundRobinScheduling :: mark,Function.identity()));
	}

	@Override
	public JpaRepository<Route, Long> getRepository() {
		// TODO Auto-generated method stub
		return routeRepository;
	}

	@Override
	public Map<String, String> findReqUrl(Long galleryId, Long productId) {
		// TODO Auto-generated method stub
		return routeRepository.findReqUrl(galleryId, productId);
	}
	
	public Gallery getGallery(String productMark,BigDecimal amount){
		RoundRobinSchedulings.get(productMark).setAmount(amount);
		return RoundRobinSchedulings.get(productMark).getGallery();
	}

}
