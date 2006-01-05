package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.opensymphony.xwork.util.OgnlValueStack;

/**
 * <class-comment/>
 */
public class WizardInterceptor extends AroundInterceptor {
    
    protected void after(ActionInvocation dispatcher, String result) throws Exception {
    }

    protected void before(ActionInvocation invocation) throws Exception {
        Action action = invocation.getAction();

        if (action instanceof WizardAction) {
            WizardAction wizardAction = (WizardAction) action;
            OgnlValueStack stack = invocation.getStack();
            stack.push(wizardAction.getWizard());
            stack.push(wizardAction.getCurrentState());
        }
    }
}
