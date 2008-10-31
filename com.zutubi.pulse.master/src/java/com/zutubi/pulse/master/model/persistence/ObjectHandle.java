package com.zutubi.pulse.master.model.persistence;

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

    public boolean equals(Object o)
    {
        if ((o == null) || !(o instanceof ObjectHandle))
        {
            return false;
        }
        ObjectHandle otherHandle = (ObjectHandle) o;
        return otherHandle.id.equals(id) && otherHandle.clazz == clazz;
    }

    public int hashCode()
    {
        return id.hashCode();
    }
}
