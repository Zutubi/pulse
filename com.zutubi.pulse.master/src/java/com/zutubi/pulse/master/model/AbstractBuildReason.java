package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Abstract parent for classes that can describe why a build occured (i.e.
 * why the request was triggered).
 */
public abstract class AbstractBuildReason extends Entity implements BuildReason, Cloneable
{
    public boolean isUser()
    {
        return false;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public String getTriggerName()
    {
        return null;
    }
}
