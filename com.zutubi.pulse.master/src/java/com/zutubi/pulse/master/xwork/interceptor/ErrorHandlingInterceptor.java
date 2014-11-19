package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.io.EofException;

/**
 * An xwork interceptor implementation that takes some of the commonly raised
 * exceptions and translates them into the appropriate action responses.
 */
public class ErrorHandlingInterceptor implements Interceptor
{
    private static final Logger LOG = Logger.getLogger(ErrorHandlingInterceptor.class);

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
        catch (IllegalArgumentException e)
        {
            // This exception is used by the config system to prevent against
            // invalid input.
            LOG.info(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            action.addActionError(e.getMessage());
            return Action.ERROR;
        }
        catch (LookupErrorException e)
        {
            LOG.info(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            action.addActionError(e.getMessage());
            return Action.ERROR;
        }
        catch (EofException e)
        {
            // Harmless
            return Action.NONE;
        }
    }
}
