package com.nadia.pos.dao;

import com.nadia.pos.model.BaseEntity;
import java.util.List;
import java.util.Optional;

public interface BaseDAO<T extends BaseEntity> {
    T save(T entity);
    Optional<T> findById(Long id);
    List<T> findAll();
    void delete(T entity);
    void deleteById(Long id);
    boolean exists(Long id);
    void update(T entity);
}
