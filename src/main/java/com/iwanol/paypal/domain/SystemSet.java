package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * 系统设置实体
 * @author leo
 *
 */
@Entity
public class SystemSet implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	@Column(length=25)
	@NotNull(message="网站名称不能为空!")
	@Length(min=4,max=25,message="网站长度4到25位之间！")
	private String webName; //网站名称
	
	@Column(length=30)
	@NotNull(message="主站不能为空!")
	private String domainName; //主站域名
	
	@Column(length=30,nullable = false)
	private String payDomainName; //接口域名
	
	private String webCopyRight; //网站版权信息
	
	@Column(name="service_qq",length=11)
	@Length(min=5,max=11,message="qq长度必须在5到11位之间")
	private String serviceQQ; //客服qq
	
	@Column(length=11)
	private String servicePhone;
	
	@Column(nullable = false)
	private Boolean state; //false/0 关闭系统日志监控  true/1开启系统日志监控
	
	@Column(nullable = false)
	private Boolean routeState; // 系统通道轮循开启状态
	
	private Integer settleType; //结算类型 0 t0 1 t1
	
	private BigDecimal t0MinSettle; //t0 最小结算金额
	
	private BigDecimal t1MinSettle; //t1最小结算金额
	
	private Integer t0SettleCount; // t0 一天最多结算次数
	
	@Column(length = 2)
	private BigDecimal tailAmountScope = new BigDecimal("0.5").setScale(1); //风控金额范围

	public SystemSet() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWebName() {
		return webName;
	}

	public void setWebName(String webName) {
		this.webName = webName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getPayDomainName() {
		return payDomainName;
	}

	public void setPayDomainName(String payDomainName) {
		this.payDomainName = payDomainName;
	}

	public String getWebCopyRight() {
		return webCopyRight;
	}

	public void setWebCopyRight(String webCopyRight) {
		this.webCopyRight = webCopyRight;
	}

	public String getServiceQQ() {
		return serviceQQ;
	}

	public void setServiceQQ(String serviceQQ) {
		this.serviceQQ = serviceQQ;
	}

	public String getServicePhone() {
		return servicePhone;
	}

	public void setServicePhone(String servicePhone) {
		this.servicePhone = servicePhone;
	}

	public Integer getSettleType() {
		return settleType;
	}

	public void setSettleType(Integer settleType) {
		this.settleType = settleType;
	}

	public BigDecimal getT0MinSettle() {
		return t0MinSettle;
	}

	public void setT0MinSettle(BigDecimal t0MinSettle) {
		this.t0MinSettle = t0MinSettle;
	}

	public BigDecimal getT1MinSettle() {
		return t1MinSettle;
	}

	public void setT1MinSettle(BigDecimal t1MinSettle) {
		this.t1MinSettle = t1MinSettle;
	}

	public Integer getT0SettleCount() {
		return t0SettleCount;
	}

	public void setT0SettleCount(Integer t0SettleCount) {
		this.t0SettleCount = t0SettleCount;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public Boolean getRouteState() {
		return routeState;
	}

	public void setRouteState(Boolean routeState) {
		this.routeState = routeState;
	}

	public BigDecimal getTailAmountScope() {
		return tailAmountScope;
	}

	public void setTailAmountScope(BigDecimal tailAmountScope) {
		this.tailAmountScope = tailAmountScope;
	}
	
}
