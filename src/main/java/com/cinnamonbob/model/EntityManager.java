package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

/**
 *
 *
 */
public interface EntityManager<T extends Entity>
{
    void save(T entity);
    void delete(T entity);
}
