package com.iwanol.paypal.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.iwanol.paypal.domain.Route;

public interface RouteRepository extends JpaRepository<Route,Long> {
	
	List<Route> findByStateAndProductProductMark(Boolean state,String productMark);
	
	List<Route> findByStateAndProductProductMarkAndGalleryMinAmountIsLessThanAndGalleryMaxAmountGreaterThanEqual(Boolean state,String productMark,BigDecimal minAmount,BigDecimal maxAmount);
	@Query("select r.url as url "
			+ "from Route r "
			+ "where r.gallery.id = ?1 "
			+ "and r.product.id = ?2")
	Map<String,String> findReqUrl(Long galleryId,Long productId);

}
