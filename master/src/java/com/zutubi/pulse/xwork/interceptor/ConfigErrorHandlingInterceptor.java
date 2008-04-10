package com.zutubi.pulse.xwork.interceptor;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.util.logging.Logger;

/**
 */
public class ConfigErrorHandlingInterceptor implements Interceptor
{
    private static final Logger LOG = Logger.getLogger(ConfigErrorHandlingInterceptor.class);

    public void destroy()
    {
    }

    public void init()
    {
    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        try
        {
            return invocation.invoke();
        }
        catch(Exception e)
        {
            LOG.info(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            action.addActionError(e.getMessage());
            return Action.ERROR;
        }
    }
}
