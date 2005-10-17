package com.cinnamonbob.model.persistence;

import java.io.Serializable;

/**
 * <class-comment/>
 */
public class ObjectHandle
{
    public Serializable id;
    public Class clazz;

    public ObjectHandle()
    {

    }

    public ObjectHandle(Serializable id, Class clazz)
    {
        this.id = id;
        this.clazz = clazz;
    }
}
