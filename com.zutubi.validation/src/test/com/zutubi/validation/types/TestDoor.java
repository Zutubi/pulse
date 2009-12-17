package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class TestDoor
{
    private Object handle;

    @Required public Object getHandle()
    {
        return handle;
    }

    public void setHandle(Object handle)
    {
        this.handle = handle;
    }
}
