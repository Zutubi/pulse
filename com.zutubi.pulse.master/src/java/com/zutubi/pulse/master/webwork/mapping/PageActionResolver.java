package com.zutubi.pulse.master.webwork.mapping;

/**
 */
public class PageActionResolver extends ParameterisedActionResolver
{
    public PageActionResolver(String action, String page)
    {
        super(action);
        addParameter("startPage", page);
    }
}
