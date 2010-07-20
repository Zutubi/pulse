package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.model.Entity;

import java.util.List;

/**
 * 
 *
 */
public interface EntityDao<T extends Entity>
{
    T findById(long id);

    <U extends T> U findByIdAndType(long id, Class<U> type);

    /**
     * Force a flush of the current changes to the database.
     */
    void flush();

    List<T> findAll();

    void save(T entity);

    void delete(T entity);

    void refresh(T entity);

    int count();
}
