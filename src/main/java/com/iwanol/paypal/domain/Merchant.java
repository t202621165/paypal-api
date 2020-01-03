package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import com.iwanol.paypal.util.CommonUtil;

/**
 * 平台商户实体
 * @author leo
 *
 */
@Entity
public class Merchant implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 主键id
	
	@Column(length=14,nullable=false,unique=true)
	private String account; //商户账号   唯一  
	
	@Column(length=64,nullable=false)
	private String passWord; //商户登陆密码
	
	@Temporal(TemporalType.DATE)
	private Date regDate; //商户注册时间
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastDate; //商户最后登陆时间
	
	@Column(length=10,unique=true)
	@NotNull(message="昵称不能为空!")
	private String nickName; //商户昵称
	
	@Column(length=30)
	private String iconUrl; //商户头像
	
	@Column(length=32,nullable=false)
	private String merchantKey; //商户密钥
	
	@Column(length=1)
	private Integer state = 0; //商户状态 0 未激活 1 已激活 2系统禁用 3.关闭支付下单功能
	
	@Email(message="邮箱格式不正确!")
	@Column(length=30)
	@NotNull(message="邮箱不能为空!")
	private String email; //商户邮箱
	
	@Column(nullable=false)
	private Boolean certificationState ; //商户认证状态 false/0未认证  true/1已认证
	
	@Column(nullable=false)
	private Boolean bankBindState ; //银行卡绑定状态 false/0未绑定  true/1已绑定
	
	@Column(length=11)
	private String telPhone; //商户手机号码
	
	@Column(length=50)
	private String company; //商户公司名称
	
	@Column(nullable=false)
	private Boolean type; // false/0 个人商户  true/1企业商户
	
	private Integer settlementType; //商户结算类型 0 为 T0 1为T1;
	
	@Column(nullable=false)
	private Boolean agency ;// true/1 为代理  false/0 非代理 
	
	private String retUrl; //同步返回地址
	
	private String notifyUrl; //异步通知地址
	
	@Column(length=25)
	private String organizationNumber;//组织机构代码
	
	private BigDecimal fee; //单笔提现手续费 默认5.00元
	
	private Boolean isOpenTail = Boolean.FALSE; // 是否开启尾额
	
	private Integer tailRatio = 0; //风控分成
	
	@Column(nullable = false)
	private BigDecimal minAmount;
	
	@Column(nullable = false)
	private BigDecimal maxAmount;
	
	@OneToMany(mappedBy="merchant",cascade={CascadeType.REMOVE})
	private Set<PlatformOrder> platformOrders = new HashSet<PlatformOrder>(); //一个商户对应多个订单
	
	
	@OneToMany(mappedBy="merchant",cascade={CascadeType.REMOVE})
	private Set<ProductRate> productRates = new HashSet<ProductRate>(); //商户产品费率
	
	@OneToMany(mappedBy="merchant",cascade=CascadeType.REMOVE)
	private Set<MerchantProductGallery> merchantProductGallerys = new HashSet<MerchantProductGallery>();

	
	@OneToMany(mappedBy="merchant",cascade={CascadeType.REMOVE})
	private Set<Bank> banks = new HashSet<Bank>();//商户银行卡
	
	@ManyToOne(fetch=FetchType.LAZY)
	private MerchantBusiness merchantBusiness = new MerchantBusiness();
	
	@ManyToOne
	private Merchant merchant;
	
	@OneToMany(mappedBy="merchant")
	private List<Merchant> children = new ArrayList<Merchant>();
	
	/**
	 * 初始化值
	 */
	public void init(){
		this.regDate = new Date();
		this.account = CommonUtil.getCommonUtil().createAccount(4);// 创建账号
		this.agency = false;
		this.bankBindState = false;
		this.certificationState = false;
		this.type = false;
		this.fee = new BigDecimal(5.00);
		this.lastDate = new Date();
		this.settlementType = 1;
		this.state = 0;
	}

	public Merchant() {
		super();
	}
	
	public Merchant(Long id) {
		this.id = id;
	}
	
	public Merchant(Integer state,Long count) {
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getMerchantKey() {
		return merchantKey;
	}

	public void setMerchantKey(String merchantKey) {
		this.merchantKey = merchantKey;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	

	public String getTelPhone() {
		return telPhone;
	}

	public void setTelPhone(String telPhone) {
		this.telPhone = telPhone;
	}

	

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}


	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Boolean getCertificationState() {
		return certificationState;
	}

	public void setCertificationState(Boolean certificationState) {
		this.certificationState = certificationState;
	}

	public Boolean getBankBindState() {
		return bankBindState;
	}

	public void setBankBindState(Boolean bankBindState) {
		this.bankBindState = bankBindState;
	}

	public Boolean getType() {
		return type;
	}

	public void setType(Boolean type) {
		this.type = type;
	}

	public Integer getSettlementType() {
		return settlementType;
	}

	public void setSettlementType(Integer settlementType) {
		this.settlementType = settlementType;
	}

	public Boolean getAgency() {
		return agency;
	}

	public void setAgency(Boolean agency) {
		this.agency = agency;
	}

	public String getRetUrl() {
		return retUrl;
	}

	public void setRetUrl(String retUrl) {
		this.retUrl = retUrl;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getOrganizationNumber() {
		return organizationNumber;
	}

	public void setOrganizationNumber(String organizationNumber) {
		this.organizationNumber = organizationNumber;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public Boolean getIsOpenTail() {
		return isOpenTail;
	}

	public void setIsOpenTail(Boolean isOpenTail) {
		this.isOpenTail = isOpenTail;
	}

	public Set<PlatformOrder> getPlatformOrders() {
		return platformOrders;
	}

	public void setPlatformOrders(Set<PlatformOrder> platformOrders) {
		this.platformOrders = platformOrders;
	}

	public Set<ProductRate> getProductRates() {
		return productRates;
	}

	public void setProductRates(Set<ProductRate> productRates) {
		this.productRates = productRates;
	}

	public Set<MerchantProductGallery> getMerchantProductGallerys() {
		return merchantProductGallerys;
	}

	public void setMerchantProductGallerys(Set<MerchantProductGallery> merchantProductGallerys) {
		this.merchantProductGallerys = merchantProductGallerys;
	}

	public Set<Bank> getBanks() {
		return banks;
	}

	public void setBanks(Set<Bank> banks) {
		this.banks = banks;
	}	

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
	
	public MerchantBusiness getMerchantBusiness() {
		return merchantBusiness;
	}

	public void setMerchantBusiness(MerchantBusiness merchantBusiness) {
		this.merchantBusiness = merchantBusiness;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public List<Merchant> getChildren() {
		return children;
	}

	public void setChildren(List<Merchant> children) {
		this.children = children;
	}

	public BigDecimal getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(BigDecimal minAmount) {
		this.minAmount = minAmount;
	}

	public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}

	public Integer getTailRatio() {
		return tailRatio;
	}

	public void setTailRatio(Integer tailRatio) {
		this.tailRatio = tailRatio;
	}
	
	/*@Override  
	public String toString() {
	    return this.account;  
	}  
	  
	@Override  
	public int hashCode() {  
	    return nickName.hashCode();  
	}
	  
	@Override  
	public boolean equals(Object obj) {
	    return this.toString().equals(obj.toString());  
	}  */
}
