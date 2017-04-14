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

package com.zutubi.pulse.master.xwork.results;

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
