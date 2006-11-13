package com.zutubi.pulse.form.persistence;

/**
 * <class-comment/>
 */
public interface ObjectRegistry
{
    Class findType(String key);

    void register(String key, Class type);

    void unregister(String key);
}
