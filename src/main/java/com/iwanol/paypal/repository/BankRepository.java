package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.Bank;

public interface BankRepository extends JpaRepository<Bank,Long> {
	Bank findByMerchantIdAndBankType(Long merchantId,Boolean bankType);
}
