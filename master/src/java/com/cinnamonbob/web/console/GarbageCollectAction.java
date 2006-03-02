package com.cinnamonbob.web.console;

import com.cinnamonbob.web.ActionSupport;

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
