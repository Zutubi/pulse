/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.wizard;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.xwork.TextProviderSupport;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Usage:
 * <p/>
 * <action name="wizard" class="com.zutubi.pulse.web.wizard.WizardAction">
 * <param name="wizard">com.zutubi.pulse.web.wizard.example.ExampleWizard</param>
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
     * The submit field value is used as an override for the next, previous and
     * cancel fields, and is set by a javascript function when the user hits enter
     * on a form. Without this, the first submit button would always be the one used. 
     */
    private String submit;

    /**
     * Local text provider, used in to retrieve the i18n text based on the wizard class,
     * not this action class.
     */
    private transient TextProvider textProvider = null;

    /**
     * The system object factory.
     */
    private ObjectFactory objectFactory;

    /**
     * Indicate whether or not lazy initialisation of the wizard is required. We can not
     * initialise the wizard when it is created since not all of the interceptors will have been
     * executed at that point. We need to wait until the action is being processed.
     */
    private boolean requiresInitialisation = false;

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

    /**
     *
     * @param submit
     */
    public void setSubmit(String submit)
    {
        this.submit = submit;
    }

    public boolean isCancelSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("cancel");
        }
        else
        {
            return TextUtils.stringSet(cancel);
        }
    }

    public boolean isPreviousSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("previous");
        }
        else
        {
            return TextUtils.stringSet(previous);
        }
    }

    public boolean isNextSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("next");
        }
        else
        {
            return TextUtils.stringSet(next);
        }
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

    /**
     * We can not initialise the wizard when we first create it since at that
     * point, the wizard and the initial state have NOT been through the registered
     * interceptors - intial values have not been set. Therefore, we use the
     * requiresInitialisation field to indicate that init is required, and delay
     * the initialisation until the wizard is called.
     */
    private void initIfRequired()
    {
        if (wizard == null)
        {
            // ensure that the wizard has been retrieved.
            getWizard();
        }
        if (requiresInitialisation)
        {
            wizard.initialise();
            requiresInitialisation = false;
        }
    }

    public String doInput()
    {
        try
        {
            initIfRequired();
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
            initIfRequired();
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
            initIfRequired();
            // always attempt to move to the next state.
            if (wizard.hasErrors())
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
            initIfRequired();
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
            initIfRequired();

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
                Wizard wizardInstance = objectFactory.buildBean(wizardClass);
                wizardInstance.setLocaleProvider(this);
                requiresInitialisation = true;
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

    /**
     * The object factory is required to handle the wizard instantiation.
     *
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * make the state directly available to the ognl stack.
     */
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
     * <p/>
     * NOTE: DO NOT CHANGE THIS METHOD SIGNATURE, YOU WILL BREAK ALL OF THE EXISTING WIZARDS.
     */
    public String getState()
    {
        return getCurrentStateName();
    }

    //---( custom handling of the TextProvider interface )---

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

    public String getText(String aTextName)
    {
        return getTextProvider().getText(aTextName);
    }

    public String getText(String aTextName, String defaultValue)
    {
        return getTextProvider().getText(aTextName, defaultValue);
    }

    public String getText(String aTextName, List args)
    {
        return getTextProvider().getText(aTextName, args);
    }

    public String getText(String aTextName, String defaultValue, List args)
    {
        return getTextProvider().getText(aTextName, defaultValue, args);
    }

    public ResourceBundle getTexts(String aBundleName)
    {
        return getTextProvider().getTexts(aBundleName);
    }

    public String getText(String key, String defaultValue, List args, OgnlValueStack stack)
    {
        return getTextProvider().getText(key, defaultValue, args, stack);
    }

    public void clearErrors()
    {
        WizardState currentState = getCurrentState();
        // the current state is null before the wizard has been initialised.
        if (currentState != null)
        {
            getWizard().clearErrors();
        }
    }
}
