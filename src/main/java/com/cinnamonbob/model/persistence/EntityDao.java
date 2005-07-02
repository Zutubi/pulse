package com.cinnamonbob.model.persistence;

import com.cinnamonbob.model.Entity;

import java.util.List;

/**
 * 
 *
 */
public interface EntityDao
{
    Entity findById(long id);
    
    List findAll();
    
    void save(Entity entity);
}
