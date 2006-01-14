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
    private Wizard wizard;

    private String cancel;
    private String previous;

    public void setWizardClass(String wizardClass)
    {
        this.wizardClass = wizardClass;
    }

    public void setCancel(String str)
    {
        this.cancel = str;
    }

    public void setPrevious(String previous)
    {
        this.previous = previous;
    }

    public boolean isCancelled()
    {
        return TextUtils.stringSet(cancel);
    }

    private boolean isPrevious()
    {
        return TextUtils.stringSet(previous);
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
        try
        {
            return doExecute();
        }
        catch (RuntimeException e)
        {
            // remove the wizard from the session so that we can start fresh
            Map session = ActionContext.getContext().getSession();
            session.remove(wizardClass);
            return "error";
        }
    }

    public String doExecute()
    {
        Map session = ActionContext.getContext().getSession();
        Wizard wizard = getWizard();

        if (isCancelled())
        {
            // clean out session.
            wizard.cancel();
            session.remove(wizardClass);
            return "cancel";
        }

        if (isPrevious())
        {
            return wizard.traverseBackward();
        }

        String nextState = wizard.traverseForward();
        if (wizard.isComplete())
        {
            session.remove(wizardClass);
        }
        return nextState;
    }
//        // lookup wizard.
//        if (isInitialRequest())
//        {
//            // this is the first time this wizard is being executed.
//            WizardState initialState = wizard.getCurrentState();
//            initialState.initialise();
//            return initialState.getStateName();
//        }
//
//        WizardState currentState = wizard.getCurrentState();
//
//        // validate the current state.
//        doValidation(currentState);
//        if (currentState.hasErrors())
//        {
//            // if there are validation errors, we want to stay in the current state.
//            return currentState.getStateName();
//        }
//
//        currentState.execute();
//        WizardState next = wizard.getState(currentState.getNextState());
//
//        if (next == null)
//        {
//            // The wizard is complete.
//            // - remove wizard from the session so that it can be executed again.
//            wizard.process();
//            wizard.setCurrentState(null);
//            session.remove(wizardClass);
//            return SUCCESS;
//        }
//
//        wizard.setCurrentState(next);
//
//        next.initialise();
//
//        return next.getStateName();

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
