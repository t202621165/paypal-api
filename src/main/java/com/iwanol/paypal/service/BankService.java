package com.iwanol.paypal.service;

import com.iwanol.paypal.domain.Bank;

public interface BankService {
	Bank findByMerchantIdAndBankType(Long merchantId,Boolean bankType);
}
