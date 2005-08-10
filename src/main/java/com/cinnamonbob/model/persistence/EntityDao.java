package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Entity;

import java.util.List;

/**
 * 
 *
 */
public interface EntityDao<T extends Entity>
{
    T findById(long id);
    
    List<T> findAll();
    
    void save(T entity);
    void delete(T entity);
    void refresh(T entity);
}
