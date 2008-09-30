package com.zutubi.pulse.xwork.results;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * A special result type used to send an appropriate HTTP response to Ajax
 * requests run while Pulse is starting up.  The front end can either
 * ignore errors (appropriate for auto-refresh) or show some basic status to
 * the user (appropriate for user-initiated requests).
 */
public class SystemStartingAjaxResult implements Result
{
    public void execute(ActionInvocation actionInvocation) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("text/plain");
        response.getWriter().write("Pulse is currently starting up, please retry in a few minutes...");
        response.getWriter().flush();
    }
}
