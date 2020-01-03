package com.iwanol.paypal.base.service.impl;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.iwanol.paypal.base.service.BaseService;
@Service
public abstract class BaseServiceImpl<T> implements BaseService<T> {
	
	public abstract JpaRepository<T, Long> getRepository();

	@Override
	@Transactional
	public T saveEntity(T t) {
		// TODO Auto-generated method stub
		return getRepository().save(t);
	}

	@Override
	@Transactional
	public T updateEntity(T t) {
		// TODO Auto-generated method stub
		return getRepository().save(t);
	}

	@Override
	@Transactional
	public void deleteEntity(Long id) {
		// TODO Auto-generated method stub
		getRepository().deleteById(id);
	}

	@Override
	@Transactional
	public void deleteEntity(T t) {
		// TODO Auto-generated method stub
		getRepository().delete(t);
	}

	@Override
	@Transactional
	public void deleteEntityInBatch(List<T> t) {
		// TODO Auto-generated method stub
		getRepository().deleteInBatch(t);
	}

	@Override
	public Optional<T> findEntity(Long id) {
		// TODO Auto-generated method stub
		return getRepository().findById(id);
	}

	@Override
	public List<T> findEntitys(List<Long> ids) {
		// TODO Auto-generated method stub
		return getRepository().findAllById(ids);
	}

	@Override
	public List<T> findEntitys() {
		// TODO Auto-generated method stub
		return getRepository().findAll();
	}

}
