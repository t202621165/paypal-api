package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 消息推送实体
 * @author leo
 *
 */
@Entity
public class Notice implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	@Temporal(TemporalType.DATE)
	private Date sendDate; //消息推送日期
	
	@Column(length=25)
	@Size(min=4,max=25,message="标题在4到25个字之间")
	private String title;
	
	@Column(columnDefinition="text")
	private String content; //消息内容

	@Column(nullable=false)
	private Boolean state; //消息状态 false/0 不启用  true/1 启用 

	@Column(nullable=false)
	private Boolean isRead; // false/0未读 true/1已读

	@Column(nullable=false)
	private Boolean sendType; // false/0  推送到平台 ture 1 推送到邮箱
	
	@JsonIgnore
	@OneToOne
	private User user;

	public Notice() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Boolean getSendType() {
		return sendType;
	}

	public void setSendType(Boolean sendType) {
		this.sendType = sendType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
	
}
