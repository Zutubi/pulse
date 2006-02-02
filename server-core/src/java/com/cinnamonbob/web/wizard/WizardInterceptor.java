package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.util.OgnlValueStack;

/**
 * <class-comment/>
 */
public class WizardInterceptor extends AroundInterceptor {
    
    protected void before(ActionInvocation invocation) throws Exception {
        Action action = invocation.getAction();

        if (action instanceof WizardAction) {
            WizardAction wizardAction = (WizardAction) action;
            OgnlValueStack stack = invocation.getStack();
            stack.push(wizardAction.getWizard());
            stack.push(wizardAction.getCurrentState());
            invocation.addPreResultListener(new PreResultListener()
            {
                public void beforeResult(ActionInvocation invocation, String resultCode)
                {
                    WizardAction wizardAction = (WizardAction) invocation.getAction();
                    OgnlValueStack stack = invocation.getStack();
                    stack.push(wizardAction.getCurrentState());
                }
            });
        }
    }

    protected void after(ActionInvocation dispatcher, String result) throws Exception
    {
    }
}
