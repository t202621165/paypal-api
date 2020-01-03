package com.iwanol.paypal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.Payee;

public interface PayeeRepository extends JpaRepository<Payee,Long>{
	Optional<Payee> findByIsDefault(Boolean isDefault);
}
