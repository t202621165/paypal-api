package com.iwanol.paypal.payee;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Merchant;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.vo.PayeeVo;

public interface PayeeInterface {
	/**
	 * 初始化代付
	 * @param merchant
	 * @param v
	 * @return
	 */
	JSONObject init(Merchant merchant,PayeeVo v);
	/**
	 * @param merchant
	 * @return 校验商户代付金额
	 */
	JSONObject valid(Merchant merchant,String amount);
	
	/**
	 * @return 选择代付类型
	 */
    Payee getPayee();
    
    /**
     * @return 执行代付请求
     */
    JSONObject execute(Payee payee,PayeeVo v);
}
