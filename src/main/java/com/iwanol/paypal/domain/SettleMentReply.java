package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 批复
 * @author leo
 *
 */
@Entity
public class SettleMentReply implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	@Column(length=22)
	private String serialNumber; //流水号 PF+年+月+日+时+分+秒+毫秒+4位随机数+服务器标识

	@Column(nullable=false)
	private Boolean state; //0 批复中 1 出款成功
	
	@OneToMany(mappedBy="settleMentReply",cascade={CascadeType.REMOVE})
	private Set<SettleMent> settleMents = new HashSet<SettleMent>();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date replyDate; //批复时间
	
	@OneToOne
	private User user; //批复人


	public SettleMentReply() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public Set<SettleMent> getSettleMents() {
		return settleMents;
	}

	public void setSettleMents(Set<SettleMent> settleMents) {
		this.settleMents = settleMents;
	}

	public Date getReplyDate() {
		return replyDate;
	}

	public void setReplyDate(Date replyDate) {
		this.replyDate = replyDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
