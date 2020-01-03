package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 商户银行卡实体
 * @author leo
 *
 */
@Entity
public class Bank implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	private String bankName; //开户行
	
	private String realName; //银行卡真实姓名
	
	private String bankBranch; //开户支行
	
	private String bankNumber; //银行卡账号
	
	private BigDecimal overMoney = new BigDecimal(0.00); //账户余额
	
	private BigDecimal settleMoney = new BigDecimal(0.00); //可结算余额   t1只能结算当天以前的订单利润
	
	private BigDecimal allDeposit = new BigDecimal(0.00); //总存入金额
	
	private BigDecimal allPay = new BigDecimal(0.00); //总支出金额
	
	@Column(nullable=false)
	private Boolean bankType; //银行卡类型 true/1 主卡 false/0 副卡
	
	@OneToMany(mappedBy="bank")
	private Set<SettleMent> settleMents = new HashSet<SettleMent>();
	
	@Column(nullable = false)
	private Boolean payeeState; // 是否允许代付
	
	@ManyToOne
	private Merchant merchant;
	
	public void init(){
		this.bankType = true;
	}

	public Bank() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getBankBranch() {
		return bankBranch;
	}

	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}

	public String getBankNumber() {
		return bankNumber;
	}

	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}

	public BigDecimal getOverMoney() {
		return overMoney;
	}

	public void setOverMoney(BigDecimal overMoney) {
		this.overMoney = overMoney;
	}

	public BigDecimal getAllDeposit() {
		return allDeposit;
	}

	public void setAllDeposit(BigDecimal allDeposit) {
		this.allDeposit = allDeposit;
	}

	public BigDecimal getAllPay() {
		return allPay;
	}

	public void setAllPay(BigDecimal allPay) {
		this.allPay = allPay;
	}

	public Boolean getBankType() {
		return bankType;
	}

	public void setBankType(Boolean bankType) {
		this.bankType = bankType;
	}
	@JsonIgnore
	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public BigDecimal getSettleMoney() {
		return settleMoney;
	}

	public void setSettleMoney(BigDecimal settleMoney) {
		this.settleMoney = settleMoney;
	}

	public Set<SettleMent> getSettleMents() {
		return settleMents;
	}

	public void setSettleMents(Set<SettleMent> settleMents) {
		this.settleMents = settleMents;
	}

	public Boolean getPayeeState() {
		return payeeState;
	}

	public void setPayeeState(Boolean payeeState) {
		this.payeeState = payeeState;
	}	
	
}
