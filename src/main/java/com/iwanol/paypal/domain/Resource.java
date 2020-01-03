package com.iwanol.paypal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * 系统资源实体
 * @author leo
 *
 */
@Entity
public class Resource implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; //主键id
	
	private String resourceMark; //资源标识
	
	private String resourceName; //资源名称
	
	private Integer sort; //排序
	
	@ManyToOne
	private Resource parent; //父级id
	
	private String resourceIcon; //资源ico图标
	
	@OneToMany(mappedBy="parent",cascade={CascadeType.REMOVE,CascadeType.PERSIST},fetch=FetchType.EAGER)
	private List<Resource> childern;
	
	@ManyToMany(mappedBy="resources")
	private List<Role> roles = new ArrayList<Role>();
	
	/**
	 * 清除实体关系 做级联删除
	 */
	public void clearChildern(){
		this.parent.getChildern().remove(this);
	}

	public Resource() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getResourceMark() {
		return resourceMark;
	}

	public void setResourceMark(String resourceMark) {
		this.resourceMark = resourceMark;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Resource getParent() {
		return parent;
	}

	public void setParent(Resource parent) {
		this.parent = parent;
	}

	public List<Resource> getChildern() {
		return childern;
	}

	public void setChildern(List<Resource> childern) {
		this.childern = childern;
	}

	public String getResourceIcon() {
		return resourceIcon;
	}

	public void setResourceIcon(String resourceIcon) {
		this.resourceIcon = resourceIcon;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
}
