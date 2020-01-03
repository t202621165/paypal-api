package com.iwanol.paypal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwanol.paypal.domain.AccountDetails;

public interface AccountDetailsRepository extends JpaRepository<AccountDetails,Long> {

}
