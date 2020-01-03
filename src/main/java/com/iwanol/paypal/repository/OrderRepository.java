package com.iwanol.paypal.repository;

import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.iwanol.paypal.domain.PlatformOrder;

public interface OrderRepository extends JpaRepository<PlatformOrder,Long> {
	@Query("select count(o.merchantOrderNumber) as count "
			+ "from "
			+ "PlatformOrder o "
			+ "where o.merchantOrderNumber = ?1")
	Map<String,Object> findOrderIsExit(String merchantOrder);
	
	PlatformOrder findByMerchantOrderNumber(String merchantOrder);
	
	PlatformOrder findBySysOrderNumber(String sysOrder);
	
	PlatformOrder findByPartyOrderNumber(String partyOrderNumber);
	
	@Query("select o.state as state "
			+ "from PlatformOrder o where o.sysOrderNumber = ?1")
	Map<String,Integer> findStateBySysOrder(String sysOrder);
	
	@Transactional
	@Modifying(clearAutomatically  = true)
	@Query("update PlatformOrder o "
			+ "set o.clientIp = ?1 where "
			+ "o.sysOrderNumber = ?2")
	void updateClientIp(String clientIp,String sysOrder);
	
	@Query("select o.sysOrderNumber as platform_no,"
			+ "o.merchantOrderNumber as order_no,"
			+ "o.amount as order_amout,"
			+ "o.orderDate as order_date,"
			+ "o.state as status "
			+ "from PlatformOrder o "
			+ "where o.merchantOrderNumber = ?1")
	Map<String,Object> orderQuery(String merchantOrderNumber);
}
