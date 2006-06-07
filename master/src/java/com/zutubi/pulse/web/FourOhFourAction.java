/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web;

import com.opensymphony.webwork.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * <class-comment/>
 */
public class FourOhFourAction extends ActionSupport
{
    private static final String ERROR_MESSAGE = "javax.servlet.error.message";
    private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";

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
