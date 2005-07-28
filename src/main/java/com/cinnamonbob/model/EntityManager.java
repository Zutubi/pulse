package com.cinnamonbob.model;

/**
 *
 *
 */
public interface EntityManager<T extends Entity>
{
    void save(T entity);
}
