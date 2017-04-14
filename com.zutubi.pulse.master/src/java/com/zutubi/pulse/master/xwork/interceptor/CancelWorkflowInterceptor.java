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

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;

/**
 * 
 *
 */
public class CancelWorkflowInterceptor implements Interceptor
{
    private static final String CANCEL = "cancel";

    public void init()
    {

    }

    public void destroy()
    {

    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        Object action = invocation.getAction();

        if (action instanceof Cancelable)
        {
            Cancelable cancelable = (Cancelable) action;
            if (cancelable.isCancelled())
            {
                cancelable.doCancel();
                return CANCEL;
            }
        }

        return invocation.invoke();
    }
}
