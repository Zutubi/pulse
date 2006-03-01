package com.cinnamonbob.web.wizard;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.xwork.TextProviderSupport;
import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.spring.SpringObjectFactory;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.ActionSupport;
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

    /**
     * The type of wizard that will be driving the interaction. This also becomes the
     * key with which the wizard is stored in the session.
     */
    private String wizardClass;

    /**
     * Local wizard instance, taken from the current session.
     */
    private Wizard wizard;

    /**
     * This is set to something if the user has selected the cancel action.
     */
    private String cancel;

    /**
     * This is set to something if the user has selected the next action.
     */
    private String next;

    /**
     * This is set to something if the user has selected the previous action.
     */
    private String previous;

    /**
     * Local text provider, used in to retrieve the i18n text based on the wizard class,
     * not this action class.
     */
    private transient TextProvider textProvider = null;

    /**
     * Set the wizard class.
     *
     * @param wizardClass is a fully qualified class name.
     */
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

    public boolean isCancelSelected()
    {
        return TextUtils.stringSet(cancel);
    }

    public boolean isPreviousSelected()
    {
        return TextUtils.stringSet(previous);
    }

    public boolean isNextSelected()
    {
        return TextUtils.stringSet(next);
    }

    public void validate()
    {
        if (!TextUtils.stringSet(wizardClass))
        {
            addActionError("Please specify a wizard class parameter in the xwork.xml action mapping.");
        }

        // pass the validate request on to the wizard.

        // Now, if there is a validation failure, we will need to return to the current state. This
        // will need to be handled by which ever method (execute() doInput() doDefault() has been
        // called). We dont want to add  these errors to the wizard instance since WW will then
        // return the INPUT result.
        if (isNextSelected())
        {
            // only want to be validating when we are moving forward.
            getWizard().validate();
        }
    }

    public String doInput()
    {
        try
        {
            // always return to the current state.
            return getCurrentStateName();
        }
        catch (RuntimeException e)
        {
            handleException(e);
            return ERROR;
        }
    }

    public String doPrevious()
    {
        try
        {
            // always return to the previous state.
            return wizard.traverseBackward();
        }
        catch (RuntimeException e)
        {
            handleException(e);
            return ERROR;
        }
    }

    public String doNext()
    {
        try
        {
            // always attempt to move to the next state.
            if (getCurrentState().hasErrors())
            {
                return getCurrentStateName();
            }
            String nextState = wizard.traverseForward();
            if (wizard.isComplete())
            {
                removeWizard();
            }
            return nextState;
        }
        catch (RuntimeException e)
        {
            handleException(e);
            return ERROR;
        }
    }

    public String doCancel()
    {
        try
        {
            // clean out session.
            getWizard().cancel();
            removeWizard();
            return "cancel";
        }
        catch (RuntimeException e)
        {
            handleException(e);
            return ERROR;
        }
    }

    public String execute()
    {
        try
        {
            if (isCancelSelected())
            {
                return doCancel();
            }

            if (isPreviousSelected())
            {
                return doPrevious();
            }

            if (isNextSelected())
            {
                return doNext();
            }

            // no post has been made, so default to the current state.
            return getCurrentStateName();
        }
        catch (RuntimeException e)
        {
            handleException(e);
            return ERROR;
        }
    }

    private void removeWizard()
    {
        Map session = ActionContext.getContext().getSession();
        session.remove(wizardClass);
    }

    private void handleException(RuntimeException e)
    {
        LOG.error(e.getMessage(), e);
        addActionError("Unexpected exception: " + e.getClass().getName() + ", " + e.getMessage());

        // remove the wizard from the session so that we can start fresh
        removeWizard();
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
                ObjectFactory objectFactory = new SpringObjectFactory();
                Wizard wizardInstance = objectFactory.buildBean(wizardClass);
                wizardInstance.initialise();
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

    public String getCurrentStateName()
    {
        return getWizard().getCurrentState().getStateName();
    }

    /**
     * Shortcut for the current state name, makes it easier to add a state hidden field to
     * the wizard forms.
     *
     * NOTE: DO NOT CHANGE THIS METHOD SIGNATURE, YOU WILL BREAK ALL OF THE EXISTING WIZARDS.
     */
    public String getState()
    {
        return getCurrentStateName();
    }

    /**
     * Override the text provider to use the wizard class for lookups.
     */
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

    public void clearErrors()
    {
        getCurrentState().clearErrors();
    }
}
