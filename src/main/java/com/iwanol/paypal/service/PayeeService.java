package com.iwanol.paypal.service;

import java.util.Optional;

import com.iwanol.paypal.domain.Payee;

public interface PayeeService {
	Optional<Payee> findByIsDefault(Boolean isDefault);
}
