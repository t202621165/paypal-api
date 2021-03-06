package com.iwanol.paypal.route.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iwanol.paypal.domain.Route;
import com.iwanol.paypal.repository.RouteRepository;
import com.iwanol.paypal.route.RoundRobinScheduling;
/**
 * 花呗路由
 * @author iwano
 */
@Component
public class HbpayRoute extends RoundRobinScheduling{

	@Autowired
	private RouteRepository routeRepository;

	@Override
	public List<Route> getRoutes() {
		// TODO Auto-generated method stub
		List<Route> routes = routeRepository.findByStateAndProductProductMarkAndGalleryMinAmountIsLessThanAndGalleryMaxAmountGreaterThanEqual(true,type(),getAmount(),getAmount());
		if (routes.isEmpty()){
			return routeRepository.findByStateAndProductProductMark(true,type());
		}
		return routes;
	}

	@Override
	public String type() {
		// TODO Auto-generated method stub
		return "hbpay";
	}

}
