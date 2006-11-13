package com.zutubi.pulse.web.wizard.auto;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.wizard.Wizard;

import java.util.Map;

/**
 * <class comment/>
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

        final OgnlValueStack stack = invocation.getStack();

        // short circuit the wizard if circumstances require it.
        String shortCircuit = null;

        if (action instanceof WizardAction)
        {
            WizardAction wizardAction = (WizardAction) action;

            // if an action has been requested, then we need to ensure that the state for which the action was
            // requests matches the state of the wizard. If the user has used the browser back button for example,
            // the wizard state will be out of sync.
            // NOTE: we need to go directly to the parameters here since the wizard will not have been through
            //       the params interceptor.
            final Map parameters = ActionContext.getContext().getParameters();
            boolean actionRequested = parameters.containsKey("next") ||
                    parameters.containsKey("previous") ||
                    parameters.containsKey("finish") ||
                    parameters.containsKey("cancel") ||
                    parameters.containsKey("submit");

            if (actionRequested)
            {
                // ensure state is in sync.
                if(wizardAction.isInitialised())
                {
                    String[] actualStates = (String[]) parameters.get("state");
                    String actualState = (actualStates.length > 0) ? actualStates[0] : null;
                    String expectedState = wizardAction.getState().getClass().getName();
                    if (!expectedState.equals(actualState))
                    {
                        String previousExpected = null;
                        Wizard wizard = wizardAction.getWizardInstance();
                        while (!expectedState.equals(actualState))
                        {
                            // a small safety net. When we get to the initial state, doPrevious will go no further.
                            // (atm at least). So, if we get the same state twice, we can exit.
                            if (previousExpected != null && previousExpected.equals(expectedState))
                            {
                                break;
                            }
                            wizard.doPrevious();
                            expectedState = wizard.getCurrentState().getClass().getName();
                            previousExpected = expectedState;
                        }
                    }
                }
                else
                {
                    wizardAction.addActionError(wizardAction.getText("wizard.state.lost"));
                    wizardAction.getWizardInstance().doRestart();
                    shortCircuit = "step";
                }
            }

            // ensure that the wizard is on the stack so that it receives any necessary parameters.
            stack.push(wizardAction.getWizardInstance());
        }
        return shortCircuit;
    }
}
