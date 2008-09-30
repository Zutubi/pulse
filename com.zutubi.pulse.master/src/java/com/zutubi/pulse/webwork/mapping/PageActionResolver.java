package com.zutubi.pulse.webwork.mapping;

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
