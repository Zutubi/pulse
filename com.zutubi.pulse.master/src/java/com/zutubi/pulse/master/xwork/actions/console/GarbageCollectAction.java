package com.zutubi.pulse.master.xwork.actions.console;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;

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
