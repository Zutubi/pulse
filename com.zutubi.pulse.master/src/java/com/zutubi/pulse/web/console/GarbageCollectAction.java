package com.zutubi.pulse.web.console;

import com.zutubi.pulse.web.ActionSupport;

/**
 * Trigger the system garbage collection.
 *
 */
public class GarbageCollectAction extends ActionSupport
{
    public String execute()
    {
        Runtime.getRuntime().gc();       
        return SUCCESS;
    }
}
