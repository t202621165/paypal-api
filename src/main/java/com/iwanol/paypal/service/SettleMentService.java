package com.iwanol.paypal.service;

import java.util.Date;

public interface SettleMentService {
	int updateStateAndDescBySerialNumber(String serialNumber, String description,Date commplate);
}
