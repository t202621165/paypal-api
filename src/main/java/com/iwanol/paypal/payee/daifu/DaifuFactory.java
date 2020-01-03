package com.iwanol.paypal.payee.daifu;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;


@Component
public class DaifuFactory {
	
   public static Map<String, DaiFu_Payee> daifuPayees = new ConcurrentHashMap<>();
	
	public DaifuFactory(List<DaiFu_Payee> daifuPayees) {
		DaifuFactory.daifuPayees.putAll(daifuPayees.parallelStream().collect(Collectors.toMap(DaiFu_Payee::type, Function.identity())));
	}
	
	public DaiFu_Payee createDaiFuPayee(String mark) throws Exception {
		return daifuPayees.get(mark);
	}
}
