package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * 角色实体
 * @author leo
 *
 */
@Entity
public class Role implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;  //主键id
	
	private String roleName; //角色名称
	
	private String roleDiscription; //角色描述
	
	@ManyToMany(fetch=FetchType.EAGER)
	private Set<Resource> resources = new HashSet<Resource>();
	
	@ManyToMany(mappedBy="roles",cascade={CascadeType.REMOVE})
	private Set<User> users = new HashSet<User>();

	public Role() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDiscription() {
		return roleDiscription;
	}

	public void setRoleDiscription(String roleDiscription) {
		this.roleDiscription = roleDiscription;
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
}
