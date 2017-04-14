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
