package com.zutubi.pulse.xwork.results;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A WebWork result that redirects to the parent path of the originally-
 * requested URL.  It is just like navigating "up" in the directory
 * hierarchy.
 */
public class UpResult implements Result
{
    public void execute(ActionInvocation invocation) throws Exception
    {
        ActionContext ctx = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);

        String path = request.getRequestURI();
        if(path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');
        if(index > 0)
        {
            path = path.substring(0, index + 1);
        }

        response.sendRedirect(path);
    }
}
