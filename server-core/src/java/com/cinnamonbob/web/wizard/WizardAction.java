package com.cinnamonbob.web.wizard;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.web.ActionSupport;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.TextProviderSupport;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Usage:
 * <p/>
 * <action name="wizard" class="com.cinnamonbob.web.wizard.WizardAction">
 * <param name="wizard">com.cinnamonbob.web.wizard.example.ExampleWizard</param>
 * <result name="a" type="velocity">example-a.vm</result>
 * <result name="b" type="velocity">example-b.vm</result>
 * <result name="success" type="redirect">/wizard-finished.action</result>
 * </action>
 * <p/>
 * NOTE: This action requires the 'model' to be located AFTER the 'static-params' interceptor
 * in the xwork interceptor stack. Reason: The wizard property.
 */
public class WizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(WizardAction.class);
    private String wizardClass;
    private Wizard wizard;

    private String cancel;
    private String next;
    private String previous;

    private transient TextProvider textProvider = null;

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

    public void setNext(String next)
    {
        this.next = next;
    }

    public boolean isCancelled()
    {
        // Wizards manage cancelling their own way
        return false;
    }

    public boolean isWizardCancelled()
    {
        return TextUtils.stringSet(cancel);
    }

    private boolean isPrevious()
    {
        return TextUtils.stringSet(previous);
    }

    private boolean isNext()
    {
        return TextUtils.stringSet(next);
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
            return processRequest();
        }
        catch (RuntimeException e)
        {
            LOG.severe(e);
            addActionError("Unexpected exception: " + e.getClass().getName() + ", " + e.getMessage());

            // remove the wizard from the session so that we can start fresh
            removeWizard();
            return "error";
        }
    }

    private String processRequest()
    {
        Map session = ActionContext.getContext().getSession();
        Wizard wizard = getWizard();

        // if state == current wizard state, then all is well.

        // else we need to locate that state and use it, or restart the wizard because
        // something crap has happened..

        if (isWizardCancelled())
        {
            // clean out session.
            wizard.cancel();
            removeWizard();
            return "cancel";
        }

        if (isPrevious())
        {
            return wizard.traverseBackward();
        }

        if (isNext())
        {
            String nextState = wizard.traverseForward();
            if (wizard.isComplete())
            {
                session.remove(wizardClass);
            }
            return nextState;
        }

        // return current state.
        return wizard.getCurrentState().getStateName();
    }

    private void removeWizard()
    {
        Map session = ActionContext.getContext().getSession();
        session.remove(wizardClass);
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
                wizardInstance.initialise();

                // small fudge to cleanly handle the first time we run this wizard.
                session.put(wizardClass, wizardInstance);
            }
            wizard = (Wizard) session.get(wizardClass);
            return wizard;
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return null;
        }
    }

// make the state directly available to the ognl stack.

    public WizardState getCurrentState()
    {
        return getWizard().getCurrentState();
    }

    public String getState()
    {
        return getCurrentState().getStateName();
    }

    // override the text provider to handle the wizard class for lookups.
    // lazy load the textProvider so that we have the wizards class available.

    private TextProvider getTextProvider()
    {
        if (textProvider == null)
        {
            textProvider = new TextProviderSupport(getWizard().getClass(), this);
        }
        return textProvider;
    }
    
    public String getText(String aTextName) {
        return getTextProvider().getText(aTextName);
    }

    public String getText(String aTextName, String defaultValue) {
        return getTextProvider().getText(aTextName, defaultValue);
    }

    public String getText(String aTextName, List args) {
        return getTextProvider().getText(aTextName, args);
    }

    public String getText(String aTextName, String defaultValue, List args) {
        return getTextProvider().getText(aTextName, defaultValue, args);
    }

    public ResourceBundle getTexts(String aBundleName) {
        return getTextProvider().getTexts(aBundleName);
    }

    public String getText(String key, String defaultValue, List args, OgnlValueStack stack) {
        return getTextProvider().getText(key,defaultValue,args,stack);
    }
}
