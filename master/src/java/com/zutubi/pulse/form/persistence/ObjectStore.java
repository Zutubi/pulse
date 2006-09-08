package com.zutubi.pulse.form.persistence;

/**
 * <class-comment/>
 */
public interface ObjectStore
{
    void save(String key, Copyable obj);

    Copyable load(String key);

    Copyable reset(String key);
}
