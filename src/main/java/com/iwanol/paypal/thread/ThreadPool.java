package com.iwanol.paypal.thread;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iwanol.paypal.domain.PlatformOrder;
import com.iwanol.paypal.service.impl.OrderServiceImpl;
import com.iwanol.paypal.util.ConnectionUrl;
import com.iwanol.paypal.util.CoreUtil;
public class ThreadPool {
	private volatile String result;
	private volatile Integer count = 1; //计数器
	private volatile String notifyurl;
	private volatile PlatformOrder platformOrder;
	private volatile Map<String,Object> paramMap;
	private volatile OrderServiceImpl orderServiceImpl;
	private volatile Logger logger =  LoggerFactory.getLogger(this.getClass());
	
	public ThreadPool(PlatformOrder platformOrder,String notifyurl,Map<String,Object> paramMap,OrderServiceImpl orderServiceImpl){
		this.notifyurl = notifyurl;
		this.platformOrder = platformOrder;
		this.paramMap = paramMap;
		this.orderServiceImpl = orderServiceImpl;
	}
	
	public ThreadPool(){};
	
	public void cahcheThreadPool(){
		ExecutorService executorService = Executors.newCachedThreadPool();
		Runnable syncRunnable = new Runnable() {
            @Override
            public void run() {
            	String notifyUrl = "";
            	if(platformOrder.getState() == 2){
            		if(notifyurl.contains("?")){
            			notifyUrl = notifyurl+"&"+CoreUtil.getCoreUtil().formatUrlMap(paramMap, null, false, false, false);
            		}else{
            			notifyUrl = notifyurl+"?"+CoreUtil.getCoreUtil().formatUrlMap(paramMap, null, false, false, false);
            		}
            		result = ConnectionUrl.getConnectionUrl().httpRequest(notifyUrl,"GET", null,paramMap.get("charset").toString());           		
            		if(result.contains("success")){
            			platformOrder.setState(1);
            			platformOrder = orderServiceImpl.updateEntity(platformOrder);
            			logger.info("商户订单{}补发{}次成功",platformOrder.getMerchantOrderNumber(),getCount());
            		}else{
        				if(getCount() < 4){
        					//每15秒发送一次 共发送3次 没有接收不再发送
            				ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);//初始化5个线程来处理未成功的订单     
        					Runnable syncRunnable = new Runnable() {
            	                @Override
            	                public void run() {
            	                	cahcheThreadPool();
            	                	increment();
            	                }
            	            };
            	            logger.info("商户订单{}补发{}次未成功",platformOrder.getMerchantOrderNumber(),getCount());
            	            executorService.schedule(syncRunnable, 15000, TimeUnit.MILLISECONDS);
        				}	   	
            		}
            	}else if(platformOrder.getState() == 1){
            		//订单已处理成功
            		logger.info("商户订单:{}已处理成功,无需补发处理",platformOrder.getMerchantOrderNumber());
            	}else{
            		logger.info("商户订单:{}未付款,暂不进行补发处理",platformOrder.getMerchantOrderNumber());
            	}   
            }
        };
        executorService.execute(syncRunnable);
	}
	
	public synchronized void increment() {  
        count++;  
    }  
      
    private Integer getCount() {  
        return count;  
    }
	
}
