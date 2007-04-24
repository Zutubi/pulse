package com.zutubi.prototype.type.record;

/**
 * Convenient base for mutable record implementations.
 */
public abstract class AbstractMutableRecord extends AbstractRecord implements MutableRecord
{
    public void setId(long id)
    {
        putMeta(ID_KEY, Long.toString(id));
    }    
}
