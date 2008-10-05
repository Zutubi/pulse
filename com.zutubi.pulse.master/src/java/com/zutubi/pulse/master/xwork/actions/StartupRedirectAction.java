package com.zutubi.pulse.master.xwork.actions;

import com.opensymphony.webwork.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 */
public class StartupRedirectAction extends ActionSupport
{
    public String execute() throws Exception
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        String uri = (String) request.getAttribute(ERROR_REQUEST_URI);
        if(uri.contains("/ajax/"))
        {
            return ERROR;
        }

        return SUCCESS;
    }
}
