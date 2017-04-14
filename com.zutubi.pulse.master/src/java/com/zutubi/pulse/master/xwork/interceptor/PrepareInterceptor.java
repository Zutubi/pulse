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

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.ParametersInterceptor;

import java.util.List;

/**
 * <class-comment/>
 */
public class PrepareInterceptor extends ParametersInterceptor
{
    private List<String> identityParameters;

    protected boolean acceptableName(String name)
    {
        if (!super.acceptableName(name))
        {
            return false;
        }

        if (identityParameters != null)
        {
            return identityParameters.contains(name);
        }
        return false;
    }

    protected void before(ActionInvocation invocation) throws Exception
    {
        Action action = (Action) invocation.getAction();
        Preparable preparable = null;
        if (action instanceof Preparable)
        {
            preparable = (Preparable) invocation.getAction();
            identityParameters = preparable.getPrepareParameterNames();
        }

        super.before(invocation);

        identityParameters = null;

        if (preparable != null)
        {
            ((Preparable) action).prepare();
        }
    }
}
