package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.iwanol.paypal.util.CommonUtil;

/**
 * 结算表实体
 * @author leo
 *
 */
@Entity
public class SettleMent implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date applyDate; //业务申请时间
	
	@Column(length=22)
	private String serialNumber; //业务流水号  提现(TX+年+月+日+时+分+秒+毫秒+4位随机数+服务器标识) 等待出款(CK+年+月+日+时+分+秒+毫秒+4位随机数+服务器标识)
	
	private Integer state; //-2拒绝出款 -1审核失败 0 等待审核 1 出款成功 2等待出款
	
	private Integer type; //业务类型 falset/0 普通业务结算   true/1资金自提
	
	private BigDecimal amount = new BigDecimal(0.00);
	
	private BigDecimal cost = new BigDecimal(5.00);
	
	private String discription; //业务描述
	
	@Column(nullable = false)
	private Boolean replyState = false;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date complateDate; //业务完成时间
	
	@ManyToOne
	private Merchant merchant; //出款商户
	
	@ManyToOne
	private Bank bank;
	
	@ManyToOne
	private SettleMentReply settleMentReply;//出款批复
	
	@Transient
	private Date startDate;
	
	@Transient
	private Date endDate;
	
	@Transient
	private Long merchantId;
	
	@Transient
	private Long bId;
	
	@Transient
	private String bankName; //开户行
	
	@Transient
	private String realName; //银行卡真实姓名
	
	@Transient
	private String bankNumber; //银行卡账号
	
	@Transient
	private String bankMark; //银行卡标识
	
	/**
	 * 清除实体关系 做级联删除
	 */
	public void clearSettleMent(){
		this.settleMentReply.getSettleMents().remove(this);
	}
	
	public SettleMent(Long id, String serialNumber, BigDecimal amount, String bankNumber) {
		this.id = id;
		this.amount = amount;
		this.serialNumber = serialNumber;
		this.bankNumber = bankNumber;
	}
	
	public SettleMent(String payeeNumber, BigDecimal amount, String discription) {
		this.serialNumber = payeeNumber;
		this.amount = amount;
		this.discription = discription;
		this.type = 1;
		this.cost = new BigDecimal(0);
		this.applyDate = new Date();
		this.state = 0;
	}

	public SettleMent() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(Date applyDate) {
		this.applyDate = applyDate;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Integer getState() {
		return state == null ? 0 : state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDiscription() {
		return discription;
	}

	public void setDiscription(String discription) {
		this.discription = discription;
	}

	public Date getComplateDate() {
		return complateDate;
	}

	public void setComplateDate(Date complateDate) {
		this.complateDate = complateDate;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public SettleMentReply getSettleMentReply() {
		return settleMentReply;
	}

	public void setSettleMentReply(SettleMentReply settleMentReply) {
		this.settleMentReply = settleMentReply;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	

	public Boolean getReplyState() {
		return replyState;
	}

	public void setReplyState(Boolean replyState) {
		this.replyState = replyState;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}
	
	public Date getStartDate() {
		return startDate == null ? CommonUtil.getCommonUtil().stringToDate(CommonUtil.getCommonUtil().TIMESTAMP,CommonUtil.getCommonUtil().currentDateTime(CommonUtil.getCommonUtil().DATE,null)+" 00:00:00") : startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = CommonUtil.getCommonUtil().stringToDate(CommonUtil.getCommonUtil().TIMESTAMP,startDate);
	}

	public Date getEndDate() {
		return endDate == null ? CommonUtil.getCommonUtil().stringToDate(CommonUtil.getCommonUtil().TIMESTAMP,CommonUtil.getCommonUtil().currentDateTime(CommonUtil.getCommonUtil().DATE,null)+" 23:59:59") : endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = CommonUtil.getCommonUtil().stringToDate(CommonUtil.getCommonUtil().TIMESTAMP,endDate);
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Long getbId() {
		return bId;
	}

	public void setbId(Long bId) {
		this.bId = bId;
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

	public String getBankNumber() {
		return bankNumber;
	}

	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}

	public String getBankMark() {
		return bankMark;
	}

	public void setBankMark(String bankMark) {
		this.bankMark = bankMark;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
}
