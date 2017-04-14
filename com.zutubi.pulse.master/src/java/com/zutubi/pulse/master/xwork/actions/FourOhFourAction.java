/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions;

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
