package com.cinnamonbob.web.wizard;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.Map;

/**
 * <class-comment/>
 */
public class WizardInterceptor implements Interceptor
{
    public void destroy()
    {

    }

    public void init()
    {

    }

    public String intercept(ActionInvocation invocation) throws Exception
    {
        String result = before(invocation);
        if (result != null)
        {
            return result;
        }
        return invocation.invoke();
    }

    protected String before(ActionInvocation invocation) throws Exception
    {
        Object action = invocation.getAction();

        String shortCircuit = null;

        if (action instanceof WizardAction)
        {
            WizardAction wizardAction = (WizardAction) action;

            // reset validation.
            wizardAction.clearErrors();

            OgnlValueStack stack = invocation.getStack();

            // if an action has been requested, then we need to ensure that the state for which the action was
            // requests matches the state of the wizard. If the user has used the browser back button for example,
            // the wizard state will be out of sync.
            // NOTE: we need to go directly to the parameters here since the wizard will not have been through
            //       the params interceptor.
            final Map parameters = ActionContext.getContext().getParameters();
            boolean actionRequested = parameters.containsKey("next") ||
                    parameters.containsKey("previous") ||
                    parameters.containsKey("cancel") ||
                    parameters.containsKey("submit");

            if (actionRequested)
            {
                // ensure state is in sync.
                String[] actualStates = (String[]) parameters.get("state");
                String actualState = (actualStates.length > 0) ? actualStates[0] : null;
                String expectedState = wizardAction.getCurrentState().getStateName();
                if (!expectedState.equals(actualState))
                {
                    if (!wizardAction.getWizard().traverseBackwardTo(actualState))
                    {
                        shortCircuit = wizardAction.getWizard().restart();
                    }
                }
            }

            // add the necessary components to the stack so that the rest of the interceptors
            // can do there magics.
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

        return shortCircuit;
    }

    protected void after(ActionInvocation dispatcher, String result) throws Exception
    {
    }
}
