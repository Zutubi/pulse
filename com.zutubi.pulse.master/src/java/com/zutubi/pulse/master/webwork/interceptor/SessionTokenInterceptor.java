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

package com.zutubi.pulse.master.webwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.zutubi.pulse.master.webwork.SessionTokenManager;

/**
 * An interceptor that requires a valid session token to be in the post parameters of the incoming
 */
public class SessionTokenInterceptor extends AroundInterceptor
{
    private static final String METHOD_CANCEL = "cancel";
    private static final String METHOD_INPUT = "input";

    protected void after(ActionInvocation actionInvocation, String string) throws Exception
    {
    }

    protected void before(ActionInvocation actionInvocation) throws Exception
    {
        ActionProxy proxy = actionInvocation.getProxy();
        if (METHOD_INPUT.equals(proxy.getMethod()) || METHOD_CANCEL.equals(proxy.getMethod()))
        {
            return;
        }

        SessionTokenManager.validateSessionToken();
    }
}
