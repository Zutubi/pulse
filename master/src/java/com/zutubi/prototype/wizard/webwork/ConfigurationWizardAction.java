package com.zutubi.prototype.wizard.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.XWorkValidationAdapter;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationWizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ConfigurationWizardAction.class);

    /**
     * The path to the configuration type that defines this wizard.
     */
    private String path;

    protected String originalPath;

    private boolean wizardRequiresLazyInitialisation = false;

    protected ConfigurationTemplateManager configurationTemplateManager;

    /**
     * Setter for the configuration path.
     *
     * @param path identification.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

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
     * This is set to something if the user has selected the finish action.
     */
    private String finish;

    /**
     * The submit field value is used as an override for the next, previous and
     * cancel fields, and is set by a javascript function when the user hits enter
     * on a form. Without this (and the associated javascript), the first submit
     * button would always be the one used.
     */
    private String submitField;

    private ValidationManager validationManager;

    public void setCancel(String cancel)
    {
        this.cancel = cancel;
    }

    public void setNext(String next)
    {
        this.next = next;
    }

    public void setPrevious(String previous)
    {
        this.previous = previous;
    }

    public void setFinish(String finish)
    {
        this.finish = finish;
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
    }

    public boolean isInitialised()
    {
        // ensure that the wizard instance is available / instantiated.
        getWizardInstance();
        return !wizardRequiresLazyInitialisation;
    }

    public boolean isCancelSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("cancel");
        }
        else
        {
            return TextUtils.stringSet(cancel);
        }
    }

    public boolean isPreviousSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("previous");
        }
        else
        {
            return TextUtils.stringSet(previous);
        }
    }

    public boolean isNextSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("next");
        }
        else
        {
            return TextUtils.stringSet(next);
        }
    }

    public boolean isFinishSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("finish");
        }
        else
        {
            return TextUtils.stringSet(finish);
        }
    }

    private boolean validateState()
    {
        WizardState state = getState();
        if (state != null)
        {
            return state.validate(path, new XWorkValidationAdapter(this));
        }

        // The session has likely timed out, or the user has manually constructed the post.
        return false;
    }

    private boolean validateWizard()
    {
        try
        {
            Object wizard = getWizardInstance();

            ValidationContext validationContext = createValidationContext(wizard.getClass());

            // validate the form input
            validationManager.validate(wizard, validationContext);

            return !hasErrors();
        }
        catch (ValidationException e)
        {
            addActionError(e.getMessage());
            return false;
        }
    }

    private ValidationContext createValidationContext(Class subject)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);
    }

    public Messages getMessages()
    {
        return Messages.getInstance(getWizardInstance().getClass());
    }

    public String execute()
    {
        try
        {
            if (isInitialised())
            {
                getState().updateRecord(ActionContext.getContext().getParameters());
            }

            // only validate when we are moving forwards in the wizard
            if (isNextSelected() || isFinishSelected())
            {
                if (!validateState() || !validateWizard())
                {
                    // if there is a validation failure, then we stay where we are.
                    return INPUT;
                }
            }
            
            initWizardIfRequired();

            if (isCancelSelected())
            {
                return doCancel();
            }
            else if (isNextSelected())
            {
                return doNext();
            }
            else if (isPreviousSelected())
            {
                return doPrevious();
            }
            else if (isFinishSelected())
            {
                return doFinish();
            }

            return "step";
        }
        catch (Exception e)
        {
            handleException(e);
            return ERROR;
        }
    }

    private String doFinish()
    {
        getWizardInstance().doFinish();
        String newPath = ((AbstractTypeWizard) getWizardInstance()).getSuccessPath();
        removeWizard();

        originalPath = path;
        path = newPath;

        return SUCCESS;
    }

    private String doPrevious()
    {
        getWizardInstance().doPrevious();
        return "step";
    }

    private String doNext()
    {
        getWizardInstance().doNext();
        return "step";
    }

    private String doCancel()
    {
        getWizardInstance().doCancel();
        removeWizard();
        return "cancel";
    }

    protected void initWizardIfRequired()
    {
        try
        {
            Wizard wizard = getWizardInstance();
            if (wizardRequiresLazyInitialisation)
            {
                wizard.initialise();
                wizardRequiresLazyInitialisation = false;
            }
        }
        catch (Exception e)
        {
            removeWizard();
            throw (RuntimeException) e;
        }
    }

    public WizardState getState()
    {
        return getWizardInstance().getCurrentState();
    }

    private void removeWizard()
    {
        String sessionKey = PathUtils.normalizePath(this.path);
        ActionContext.getContext().getSession().remove(sessionKey);
    }

    private void handleException(Exception e)
    {
        LOG.error(e.getMessage(), e);
        addActionError("Unexpected exception: " + e.getClass().getName() + ", " + e.getMessage());

        // remove the wizard from the session so that we can start fresh
        removeWizard();
    }

    @SuppressWarnings({"unchecked"})
    public Wizard getWizardInstance()
    {
        try
        {
            // normalise the path by stripping leading and trailing '/' chars
            String sessionKey = PathUtils.normalizePath(this.path);

            Map<String, Object> session = ActionContext.getContext().getSession();
            if (!session.containsKey(sessionKey))
            {
                Wizard wizardInstance = doCreateWizard();
                session.put(sessionKey, wizardInstance);
            }
            return (Wizard) session.get(sessionKey);
        }
        catch (Exception e)
        {
            LOG.warning(e);
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    protected Wizard doCreateWizard()
    {
        AbstractTypeWizard wizardInstance = null;

        Type type = configurationTemplateManager.getType(path);
        if (type instanceof CollectionType)
        {
            type = ((CollectionType) type).getCollectionType();
        }

        Class wizardClass = ConventionSupport.getWizard(type);
        if (wizardClass != null)
        {
            try
            {
                wizardInstance = (AbstractTypeWizard) ComponentContext.createBean(wizardClass);
            }
            catch (Exception e)
            {
                LOG.warning(e);
                e.printStackTrace();
            }
        }

        if (wizardInstance == null)
        {
            wizardInstance = new SingleTypeWizard();
            ComponentContext.autowire(wizardInstance);
        }
        
        wizardInstance.setPath(path);

        wizardRequiresLazyInitialisation = true;
        return wizardInstance;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
