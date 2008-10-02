package com.zutubi.pulse.webwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.zutubi.pulse.master.monitor.Monitor;
import com.zutubi.pulse.web.restore.ExecuteRestoreAction;

/**
 *
 *
 */
public class ExecAndWaitInterceptor extends com.opensymphony.webwork.interceptor.ExecuteAndWaitInterceptor
{
    public String intercept(ActionInvocation actionInvocation) throws Exception
    {
        ActionProxy proxy = actionInvocation.getProxy();
        
        Object action = proxy.getAction();

        // should implement the 

        Monitor monitor = ((ExecuteRestoreAction)action).getMonitor();

        if (monitor.isFinished())
        {
            return "success";
        }

        return super.intercept(actionInvocation);
    }
}
