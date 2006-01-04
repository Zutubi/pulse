package com.cinnamonbob.web.wizard;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.util.TextUtils;

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
 */
public class WizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(WizardAction.class);
    private String wizardClass;

    private boolean isInitialHit = false;

    public void setWizard(String wizardClass)
    {
        this.wizardClass = wizardClass;
    }

    public void validate()
    {
        if (!TextUtils.stringSet(wizardClass))
        {
            addActionError("Please specify a wizard class parameter in the xwork.xml action mapping.");
            return;
        }
    }

    public String execute()
    {
        // lookup wizard.
        Wizard wizard = lookupWizard();
        if (isInitialHit)
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
        currentState.validate();
        if (currentState.hasErrors())
        {
            getFieldErrors().putAll(currentState.getFieldErrors());
            getActionErrors().addAll(currentState.getActionErrors());
            currentState.clearErrors();
            return currentState.getStateName();
        }

        // if not valid, return to current state.
        currentState.execute();
        WizardState next = wizard.getState(currentState.getNextState());

        if (next == null)
        {
            // The wizard is complete.
            // - remove wizard from the session so that it can be executed again.
            wizard.setCurrentState(null);
            wizard.process();
            ActionContext.getContext().getSession().remove(wizardClass);
            return SUCCESS;
        }

        wizard.setCurrentState(next);

        next.initialise();

        return next.getStateName();
    }

    public WizardState getState()
    {
        return lookupWizard().getCurrentState();
    }

    private Wizard lookupWizard()
    {
        try
        {
            Map session = ActionContext.getContext().getSession();
            if (!session.containsKey(wizardClass))
            {
                Wizard wizardInstance = (Wizard) Class.forName(wizardClass).newInstance();
                session.put(wizardClass, wizardInstance);
                isInitialHit = true;
            }
            return (Wizard) session.get(wizardClass);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return null;
        }

    }
}
