package com.zutubi.pulse.master.xwork.interceptor;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;

import javax.servlet.http.HttpServletResponse;

/**
 * An interceptor that sets headers on HTTP responses to stop the browser from
 * caching returned page fragments, or even storing them at all (so they do not
 * interfere with the back button).
 */
public class AjaxInterceptor extends AroundInterceptor
{
    protected void after(ActionInvocation actionInvocation, String string) throws Exception
    {
    }

    protected void before(ActionInvocation actionInvocation) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();

        response.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
    }
}
