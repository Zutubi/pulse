package com.zutubi.pulse.xwork.interceptor;

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
