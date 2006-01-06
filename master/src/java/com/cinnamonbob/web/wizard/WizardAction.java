package com.cinnamonbob.web.wizard;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

import java.util.Map;

/**
 * Usage:
 *
 *    <action name="wizard" class="com.cinnamonbob.web.wizard.WizardAction">
 *       <param name="wizard">com.cinnamonbob.web.wizard.example.ExampleWizard</param>
 *       <result name="a" type="velocity">example-a.vm</result>
 *       <result name="b" type="velocity">example-b.vm</result>
 *       <result name="success" type="redirect">/wizard-finished.action</result>
 *    </action>
 *
 * NOTE: This action requires the 'model' to be located AFTER the 'static-params' interceptor
 * in the xwork interceptor stack. Reason: The wizard property.
 */
public class WizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(WizardAction.class);
    private String wizardClass;

    private boolean isInitialRequest = false;

    private String cancel;

    public void setWizardClass(String wizardClass)
    {
        this.wizardClass = wizardClass;
    }

    public void setCancel(String str)
    {
        this.cancel = str;
    }

    private boolean isCancelled()
    {
        return TextUtils.stringSet(cancel);
    }

    private boolean isInitialRequest()
    {
        return isInitialRequest;
    }

    public void validate()
    {
        if (!TextUtils.stringSet(wizardClass))
        {
            addActionError("Please specify a wizard class parameter in the xwork.xml action mapping.");
        }
    }

    public String execute()
    {
        Map session = ActionContext.getContext().getSession();
        if (isCancelled())
        {
            // clean out session.
            session.remove(wizardClass);
            return "cancel";
        }

        // lookup wizard.
        Wizard wizard = getWizard();
        if (isInitialRequest())
        {
            // this is the first time this wizard is being executed.
            WizardState initialState = wizard.getCurrentState();
            initialState.initialise();
            return initialState.getStateName();
        }

        WizardState currentState = wizard.getCurrentState();

        // handle interceptor stack manually for the current state.
        // a) set params: handled by this actions interceptors - getState()...
        // b) validate

        // clear previous errors if any remain.
        currentState.clearErrors();

        currentState.validate();
        if (currentState.hasErrors())
        {
            return currentState.getStateName();
        }

        // if not valid, return to current state.
        currentState.execute();
        WizardState next = wizard.getState(currentState.getNextState());

        if (next == null)
        {
            // The wizard is complete.
            // - remove wizard from the session so that it can be executed again.
            wizard.process();
            wizard.setCurrentState(null);
            ActionContext.getContext().getSession().remove(wizardClass);
            return SUCCESS;
        }

        wizard.setCurrentState(next);

        next.initialise();

        return next.getStateName();
    }

    public Wizard getWizard()
    {
        try
        {
            Map session = ActionContext.getContext().getSession();
            if (!session.containsKey(wizardClass))
            {
                // use Object factory to create this wizard
                Wizard wizardInstance = (Wizard) Class.forName(wizardClass).newInstance();
                ComponentContext.autowire(wizardInstance);
                session.put(wizardClass, wizardInstance);
                isInitialRequest = true;
            }
            return (Wizard) session.get(wizardClass);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return null;
        }
    }

    // make the state directly available to the ognl stack.
    public Object getCurrentState()
    {
        return getWizard().getCurrentState();
    }
}
