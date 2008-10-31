package com.zutubi.pulse.master.webwork.dispatcher.mapper;

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
