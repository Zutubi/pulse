package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.Entity;

import java.util.List;

/**
 * 
 *
 */
public interface EntityDao<T extends Entity>
{
    T findById(long id);

    <U extends T> U findByIdAndType(long id, Class<U> type);

    List<T> findAll();

    void save(T entity);

    void delete(T entity);

    void refresh(T entity);
}
