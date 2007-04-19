package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class MockDoor
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
