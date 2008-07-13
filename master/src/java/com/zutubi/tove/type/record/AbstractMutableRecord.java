package com.zutubi.tove.type.record;

/**
 * Convenient base for mutable record implementations.
 */
public abstract class AbstractMutableRecord extends AbstractRecord implements MutableRecord
{
    public void setHandle(long id)
    {
        putMeta(HANDLE_KEY, Long.toString(id));
    }

    public void setPermanent(boolean permanent)
    {
        putMeta(PERMANENT_KEY, Boolean.toString(permanent));
    }
}
