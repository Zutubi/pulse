package com.zutubi.pulse.form.persist;

import com.zutubi.pulse.form.persist.PersistenceException;

import java.io.Serializable;

/**
 * <class-comment/>
 */
public interface KeyGenerator
{
    /**
     * Generate a new key for the specified object.
     *
     * @param obj
     * @return
     */
    public Serializable generate(Class clazz) throws PersistenceException;
}
