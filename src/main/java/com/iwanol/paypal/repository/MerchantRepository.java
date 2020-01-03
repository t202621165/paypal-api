package com.iwanol.paypal.repository;

import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.iwanol.paypal.domain.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant,Long> {
	
	@Query("select m.state as state "
			+ "from Merchant m "
			+ "where m.account = ?1")
	Map<String,Object> findMerchantState(String account);
	
	Merchant findByAccount(String account);
	
	@Query("select m.merchantKey as key "
			+ "from Merchant m "
			+ "where m.account = ?1")
	Map<String,String> findKeyByAccount(String account);
}
