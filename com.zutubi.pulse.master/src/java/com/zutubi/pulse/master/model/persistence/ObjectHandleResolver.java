package com.zutubi.pulse.master.model.persistence;

/**
 * <class-comment/>
 */
public abstract class ObjectHandleResolver
{
    public abstract Object resolve(ObjectHandle handle);

    public abstract Object[] resolve(ObjectHandle[] handles);
}
