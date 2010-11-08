package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.logging.Logger;
import org.mortbay.http.EOFException;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;

/**
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
        catch(AccessDeniedException e)
        {
            LOG.info(e);
            HttpServletResponse response = ServletActionContext.getResponse();
            response.sendError(401);
            return Action.ERROR;
        }
        catch(IllegalArgumentException e)
        {
            // This exception is used by the config system to prevent against
            // invalid input.
            LOG.info(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            action.addActionError(e.getMessage());
            return Action.ERROR;
        }
        catch(LookupErrorException e)
        {
            LOG.info(e);
            ActionSupport action = (ActionSupport) invocation.getAction();
            action.addActionError(e.getMessage());
            return Action.ERROR;
        }
        catch(EOFException e)
        {
            // Harmless
            return Action.NONE;
        }
    }
}
