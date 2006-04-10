package com.zutubi.pulse.web;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;

import javax.servlet.http.HttpServletResponse;

/**
 */
public class AjaxInterceptor extends AroundInterceptor
{
    protected void after(ActionInvocation actionInvocation, String string) throws Exception
    {
    }

    protected void before(ActionInvocation actionInvocation) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();

        response.setHeader("Cache-Control", "must-revalidate");
        response.setDateHeader("Expires", 0);
    }
}
