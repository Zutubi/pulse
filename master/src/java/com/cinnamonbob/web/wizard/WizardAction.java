package com.cinnamonbob.web.wizard;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.Validateable;
import com.opensymphony.xwork.validator.ActionValidatorManager;
import com.opensymphony.xwork.validator.ValidationException;

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
    private Wizard wizard;

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

    public boolean isCancelled()
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

        // validate the current state.
        doValidation(currentState);
        if (currentState.hasErrors())
        {
            // if there are validation errors, we want to stay in the current state.
            return currentState.getStateName();
        }

        currentState.execute();
        WizardState next = wizard.getState(currentState.getNextState());

        if (next == null)
        {
            // The wizard is complete.
            // - remove wizard from the session so that it can be executed again.
            wizard.process();
            wizard.setCurrentState(null);
            session.remove(wizardClass);
            return SUCCESS;
        }

        wizard.setCurrentState(next);

        next.initialise();

        return next.getStateName();
    }

    private void doValidation(WizardState currentState)
    {
        //  - first clear previous errors if any remain.
        currentState.clearErrors();

        try
        {
            ActionValidatorManager.validate(currentState, currentState.getClass().getName());
            if (Validateable.class.isAssignableFrom(currentState.getClass()))
            {
                ((Validateable)currentState).validate();
            }
        }
        catch (ValidationException e)
        {
            LOG.severe(e);
        }
    }

    public Wizard getWizard()
    {
        if (wizard != null)
        {
            return wizard;
        }

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
            wizard =  (Wizard) session.get(wizardClass);
            return wizard;
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
