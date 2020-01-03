package com.iwanol.paypal.route;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Component;

import com.iwanol.paypal.domain.Gallery;
import com.iwanol.paypal.domain.Route;
/**
 * 按权重轮询调度算法
 * 
 * @author leo
 */
@Component
public abstract class RoundRobinScheduling {

	public int currentIndex = -1; // 上一次选择的通道
	public int currentWeight; // 当前调度的权值
	public int maxWeight; // 最大权值
	public int gcdWeight; // 所有通道路由中权重最大公约数
	public int counts; // 所有通道路由中通道数量
	public BigDecimal amount; //下单金额

	public abstract List<Route> getRoutes();

	public abstract String type();
	
	public String mark(){
		return type();
	}


	/**
	 * @param a
	 * @param b
	 * @return 返回最大公约数
	 */
	private int gcd(int a, int b) {
		BigInteger b1 = new BigInteger(String.valueOf(a));
		BigInteger b2 = new BigInteger(String.valueOf(b));
		BigInteger gcd = b1.gcd(b2);
		return gcd.intValue();
	}

	/**
	 * @param routes
	 * @return 所有通道路由中权重最大公约数
	 */
	private int getGCDForRoute(List<Route> routes) {
		int w = 0;
		if (routes.size() > 0){
			for (int i = 0, len = routes.size(); i < len - 1; i++) {
				if (w == 0) {
					w = gcd(routes.get(i).getGallery().getWeight(), routes.get(i + 1).getGallery().getWeight());
				} else {
					w = gcd(w, routes.get(i + 1).getGallery().getWeight());
				}
			}
		}
		return w;
	}

	/**
	 * @param routes
	 * @return 最大权值
	 */
	private int getMaxWeightForRoute(List<Route> routes) {
		int w = 0;
		if (routes.size() > 0){
			for (int i = 0, len = routes.size(); i < len - 1; i++) {
				if (w == 0) {
					w = Math.max(routes.get(i).getGallery().getWeight(), routes.get(i + 1).getGallery().getWeight());
				} else {
					w = Math.max(w, routes.get(i + 1).getGallery().getWeight());
				}
			}
		}
		return w;
	}

	/**
	 * 初始化值
	 */
	private void init() {
		setCounts(getRoutes().size());
		setMaxWeight(getMaxWeightForRoute(getRoutes()));
		setGcdWeight(getGCDForRoute(getRoutes()));
	}

	/**
	 * 算法流程： 假设有一组通道 S = {S0, S1, …, Sn-1} 有相应的权重，变量currentIndex表示上次选择的服务器
	 * 权值currentWeight初始化为0，currentIndex初始化为-1 ，当第一次的时候返回 权值取最大的那个服务器， 通过权重的不断递减
	 * 寻找 适合的服务器返回，直到轮询结束，权值返回为0
	 */
	public final Gallery getGallery() {
		init();
		while (true) {
			setCurrentIndex((getCurrentIndex() + 1) % getCounts());
			if (getCurrentIndex() == 0) {
				setCurrentWeight(getCurrentWeight() - getGcdWeight());
				if (getCurrentWeight() <= 0) {
					setCurrentWeight(getMaxWeight());
					if (getCurrentWeight() == 0)
						return null;
				}
			}
			if (getRoutes().get(getCurrentIndex()).getGallery().getWeight() >= getCurrentWeight()) {
				return getRoutes().get(getCurrentIndex()).getGallery();
			}
		}
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public int getCurrentWeight() {
		return currentWeight;
	}

	public void setCurrentWeight(int currentWeight) {
		this.currentWeight = currentWeight;
	}

	public int getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(int maxWeight) {
		this.maxWeight = maxWeight;
	}

	public int getGcdWeight() {
		return gcdWeight;
	}

	public void setGcdWeight(int gcdWeight) {
		this.gcdWeight = gcdWeight;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
