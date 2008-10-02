package com.zutubi.pulse.master.web;

import com.opensymphony.webwork.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * <class-comment/>
 */
public class FourOhFourAction extends ActionSupport
{
    private String errorRequestUri;
    private String errorMessage;
    private int errorStatusCode;
    private Throwable errorException;

    public String getErrorRequestUri()
    {
        return errorRequestUri;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public int getErrorStatusCode()
    {
        return errorStatusCode;
    }

    public Throwable getErrorException()
    {
        return errorException;
    }

    public String execute() throws Exception
    {
        HttpServletRequest request = ServletActionContext.getRequest();

        errorRequestUri = (String) request.getAttribute(ERROR_REQUEST_URI);
        errorMessage = (String) request.getAttribute(ERROR_MESSAGE);
        errorStatusCode = (Integer) request.getAttribute(ERROR_STATUS_CODE);
        errorException = (Throwable) request.getAttribute(ERROR_EXCEPTION);
        return SUCCESS;
    }

}
