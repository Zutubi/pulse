/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.xwork.interceptor;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.opensymphony.xwork.interceptor.Interceptor;

import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.AccessDeniedException;

/**
 */
public class ErrorHandlingInterceptor implements Interceptor
{
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
            HttpServletResponse response = ServletActionContext.getResponse();
            response.sendError(401);
            return Action.ERROR;
        }
    }
}
