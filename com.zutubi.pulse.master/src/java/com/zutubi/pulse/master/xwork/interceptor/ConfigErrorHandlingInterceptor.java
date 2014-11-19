package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.io.EofException;

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
        catch(EofException e)
        {
            // Harmless
            return Action.NONE;
        }
        catch(Exception e)
        {
            LOG.severe(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            String message = e.getMessage();
            if(message == null)
            {
                message = e.getClass().getName();
            }
            action.addActionError(message);
            return Action.ERROR;
        }
    }
}
