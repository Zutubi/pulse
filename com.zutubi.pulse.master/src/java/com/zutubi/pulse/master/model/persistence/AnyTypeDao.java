package com.zutubi.pulse.master.model.persistence;

import java.util.List;

/**
 * <class-comment/>
 */
public interface AnyTypeDao
{
    List<ObjectHandle> findAll();

    List<ObjectHandle> findAll(Class persistentType);

    void save(Object entity);
}
