package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

/**
 * 管理员实体
 * @author leo
 *
 */
@Entity
public class User implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	@Column(length=6,unique=true)
	@NotNull(message="用户名不能为空!")
	@Length(min=2,max=6,message="用户名3到6位长度!")
	private String userName; //用户名
	
	@Column(length=64)
	@NotNull(message="密码不能为空!")
	private String passWord; //密码

	@ManyToMany(fetch=FetchType.LAZY)
	private Set<Role> roles = new HashSet<Role>();
	
	@OneToOne(mappedBy="user",cascade={CascadeType.REMOVE})
	private SettleMentReply settleMentReply;
	
	public User() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
}
