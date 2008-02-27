package com.zutubi.prototype.type.record;

import com.zutubi.pulse.core.config.Configuration;

/**
 * Convenient base for mutable record implementations.
 */
public abstract class AbstractMutableRecord extends AbstractRecord implements MutableRecord
{
    public void setHandle(long id)
    {
        putMeta(Configuration.HANDLE_KEY, Long.toString(id));
    }

    public void setPermanent(boolean permanent)
    {
        putMeta(Configuration.PERMANENT_KEY, Boolean.toString(permanent));
    }
}
