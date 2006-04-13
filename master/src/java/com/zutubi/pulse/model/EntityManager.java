/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;

/**
 *
 *
 */
public interface EntityManager<T extends Entity>
{
    void save(T entity);
    void delete(T entity);
}
