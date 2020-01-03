package com.iwanol.paypal.base.service;

import java.util.List;
import java.util.Optional;


public interface BaseService <T>{
	/**
	 * 新增
	 * @param t
	 * @return
	 */
	T saveEntity(T t);
	
	/**
	 * 更新实体
	 * @param t
	 * @return
	 */
	T updateEntity(T t);
	
	/**
	 * 通过ID删除实体
	 * @param id
	 */
	void deleteEntity(Long id);
	
	/**
	 * 通过实体删除
	 * @param t
	 */
	void deleteEntity(T t);
	
	/**
	 * 批量删除
	 * @param t
	 */
	void deleteEntityInBatch(List<T> t);
	
	/**
	 * 通过id查找实体
	 * @param id
	 * @return
	 */
	Optional<T> findEntity(Long id);
	
	
	/**
	 * 查找集合
	 * @param ids
	 * @return
	 */
	List<T> findEntitys(List<Long> ids);
	
	/**
	 * 查询所有实体列表
	 * @return
	 */
	List<T> findEntitys();
	
}
