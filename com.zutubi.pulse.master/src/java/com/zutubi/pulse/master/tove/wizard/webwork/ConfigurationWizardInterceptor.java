package com.zutubi.pulse.master.tove.wizard.webwork;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.master.tove.wizard.Wizard;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.StringUtils;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationWizardInterceptor implements Interceptor
{
    public static final String STATE_ID_PARAMETER = "stateId";
    public static final String SUBMIT_PARAMETER = "submitField";
    private static final String[] SUBMIT_VALUES = {"cancel", "finish", "previous", "next"};

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

        if (action instanceof ConfigurationWizardAction)
        {
            ConfigurationWizardAction wizardAction = (ConfigurationWizardAction) action;

            // if an action has been requested, then we need to ensure that the state for which the action was
            // requests matches the state of the wizard. If the user has used the browser back button for example,
            // the wizard state will be out of sync.
            // NOTE: we need to go directly to the parameters here since the wizard will not have been through
            //       the params interceptor.
            final Map parameters = ActionContext.getContext().getParameters();
            boolean actionRequested = parameterSet(parameters, SUBMIT_PARAMETER) && CollectionUtils.contains(SUBMIT_VALUES, parameters.get(SUBMIT_PARAMETER)) ||
                                      parameterSet(parameters, "cancel");

            if (actionRequested)
            {
                // ensure state is in sync.
                if (wizardAction.isInitialised())
                {
                    String[] actualStates = (String[]) parameters.get(STATE_ID_PARAMETER);
                    String actualState = (actualStates.length > 0) ? actualStates[0] : null;
                    Wizard wizard = wizardAction.getWizardInstance();
                    String expectedState = wizard.getCurrentState().getId();
                    if (!expectedState.equals(actualState))
                    {
                        String previousExpected = null;
                        while (!expectedState.equals(actualState))
                        {
                            // a small safety net. When we get to the initial state, doPrevious will go no further.
                            // (atm at least). So, if we get the same state twice, we can exit.
                            if (previousExpected != null && previousExpected.equals(expectedState))
                            {
                                break;
                            }
                            wizard.doPrevious();
                            expectedState = wizard.getCurrentState().getId();
                            previousExpected = expectedState;
                        }
                    }
                }
                else
                {
                    wizardAction.addActionError(wizardAction.getText("wizard.state.lost"));
                    wizardAction.initWizardIfRequired();
                    wizardAction.getWizardInstance().doRestart();
                    shortCircuit = "step";
                }
            }

            // ensure that the wizard is on the stack so that it receives any necessary parameters.
            stack.push(wizardAction.getWizardInstance());
        }
        return shortCircuit;
    }

    private boolean parameterSet(Map parameters, String name)
    {
        Object value = parameters.get(name);
        if(value != null)
        {
            if(value instanceof String)
            {
                return ((String)value).length() != 0;
            }

            if(value instanceof String[])
            {
                String[] array = (String[]) value;
                return array.length > 0 && StringUtils.stringSet(array[0]);
            }
        }

        return false;
    }
}
