package com.iwanol.paypal;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.util.CoreUtil;
import com.iwanol.paypal.util.MD5Util;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RefundTest {
	@Autowired
	private OrderServiceImpl orderServiceImpl;
	private TestRestTemplate rest = new TestRestTemplate();
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String refundUrl = "http://16m08v3666.iok.la:43755/api/refund";
	/**
	 * 退款
	 * @throws Exception 
	 */
	@Test
	@Transactional
	public void refund() throws Exception{	
		PlatformOrder platformOrder = orderServiceImpl.findBySysOrder("IW18062918194875004220");
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("party_order_number","2018062921001004840567455004");
		map.put("sys_order_number","IW18062918194875004220");
		map.put("discription","协商退款");
		String sign = MD5Util.getMD5Util().sign(CoreUtil.getCoreUtil().formatUrlMap(map,null,true,false,false),platformOrder.getGallery().getGalleryMD5Key(),"utf-8");
		map.put("sign",sign);
		map.put("discription",URLEncoder.encode("协商退款","utf-8"));
		String dataUrl = CoreUtil.getCoreUtil().formatUrlMap(map,null,true, false, false);
		logger.info("【数据请求串:"+refundUrl+"?"+dataUrl+"】");
		String result = rest.getForObject(refundUrl+"?"+dataUrl,String.class);
		logger.info("【返回结果:"+result+"】");
	}
	
	@Test
	public void ss(){	
		String ss = "2018062921001004840567455004^1.00^SUCCESS";
		String[] result = ss.split("\\^");
		logger.info("结果0:"+result[0]+"");
	}
}
