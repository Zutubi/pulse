package com.zutubi.pulse.xwork.interceptor;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

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
    }
}
