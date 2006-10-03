package com.zutubi.pulse.form.persist;

import com.zutubi.pulse.form.persist.PersistenceException;

import java.io.Serializable;

/**
 * <class-comment/>
 */
public interface ObjectStore
{
    public Object load(Class clazz, Serializable id) throws PersistenceException;

    // These methods are to be used by objects that have an ID field defined.

    public void save(Object obj) throws PersistenceException;

    public void saveOrUpdate(Object obj) throws PersistenceException;

    public void update(Object obj) throws PersistenceException;

    public boolean delete(Object obj) throws PersistenceException;


    // These methods are to be used by objects that do not have an ID field defined.

    public void save(Serializable id, Object obj) throws PersistenceException;

    public void update(Serializable id, Object obj) throws PersistenceException;

    public boolean delete(Serializable id, Class clazz) throws PersistenceException;

    // id support.

    public boolean hasId(Class clazz);

    public Serializable generateId(Class clazz) throws PersistenceException;
}
