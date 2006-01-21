package com.cinnamonbob.xwork.interceptor;

import com.opensymphony.xwork.Action;
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
        Action action = invocation.getAction();

        if (action instanceof Cancelable) {
            Cancelable cancelable = (Cancelable) action;
            if (cancelable.isCancelled())
            {
                return CANCEL;
            }
        }

        return invocation.invoke();
    }
}
