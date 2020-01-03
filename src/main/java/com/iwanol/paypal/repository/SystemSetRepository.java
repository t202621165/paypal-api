package com.iwanol.paypal.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.iwanol.paypal.domain.SystemSet;

public interface SystemSetRepository extends JpaRepository<SystemSet,Long> {
	@Query(value = "select tail_amount_scope from system_set where mark = ?1",nativeQuery = true)
	BigDecimal findTailScope(String mark);
}
