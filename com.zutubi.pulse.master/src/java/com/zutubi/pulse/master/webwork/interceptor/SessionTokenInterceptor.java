package com.zutubi.pulse.master.webwork.interceptor;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.zutubi.pulse.master.tove.webwork.ToveActionSupport;
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

        Object action = actionInvocation.getAction();
        if (action instanceof ToveActionSupport)
        {
            ToveActionSupport toveAction = (ToveActionSupport) action;
            if (toveAction.isInputSelected() || toveAction.isCancelSelected())
            {
                return;
            }
        }

        SessionTokenManager.validateSessionToken();
    }
}
