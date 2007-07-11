package com.zutubi.prototype.wizard.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.*;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationWizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(ConfigurationWizardAction.class);

    private static final String CREATE_TEMPLATE = "template";

    private static final String SUBMIT_CANCEL = "cancel";
    private static final String SUBMIT_PREVIOUS = "previous";
    private static final String SUBMIT_NEXT = "next";
    private static final String SUBMIT_FINISH = "finish";

    /**
     * The path to the configuration type that defines this wizard.
     */
    private String path;
    protected String originalPath;
    /**
     * The submit field is used to communicate the selected button from the
     * client JavaScript.  On init, it may also indicate that we are
     * configuring a template.
     */
    private String submitField;

    private boolean wizardRequiresLazyInitialisation = false;

    private ValidationManager validationManager;
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

    public boolean isSelected(String value)
    {
        return value.equals(submitField);
    }

    private boolean validateState()
    {
        WizardState state = getState();
        if (state != null)
        {
            try
            {
                return state.validate(path, this);
            }
            catch (TypeException e)
            {
                addActionError(e.getMessage());
                return false;
            }
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
            if (isSelected(SUBMIT_NEXT) || isSelected(SUBMIT_FINISH))
            {
                if (!validateState() || !validateWizard())
                {
                    // if there is a validation failure, then we stay where we are.
                    return INPUT;
                }
            }

            initWizardIfRequired();

            if (isSelected(SUBMIT_CANCEL))
            {
                return doCancel();
            }
            else if (isSelected(SUBMIT_NEXT))
            {
                return doNext();
            }
            else if (isSelected(SUBMIT_PREVIOUS))
            {
                return doPrevious();
            }
            else if (isSelected(SUBMIT_FINISH))
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

    @SuppressWarnings({ "unchecked" })
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

    @SuppressWarnings({ "unchecked" })
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

        wizardInstance.setParameters(path, isSelected(CREATE_TEMPLATE));

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
