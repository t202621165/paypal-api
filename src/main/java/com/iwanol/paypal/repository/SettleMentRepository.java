package com.iwanol.paypal.repository;

import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.iwanol.paypal.domain.SettleMent;

public interface SettleMentRepository extends JpaRepository<SettleMent, Long>{
	@Query(value = "UPDATE SettleMent s SET s.state = 1,s.discription = ?2,s.replyState=1,s.complateDate=?3 WHERE s.serialNumber = ?1 AND s.state = 2")
	@Modifying
	@Transactional
	int updateStateAndDescBySerialNumber(String serialNumber, String description,Date commplate);
}
