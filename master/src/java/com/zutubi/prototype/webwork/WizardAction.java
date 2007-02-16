package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.validation.ValidationManager;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeException;

import java.util.Map;

/**
 *
 *
 */
public class WizardAction extends ActionSupport
{
    private static final Logger LOG = Logger.getLogger(WizardAction.class);

    /**
     * Identifier of wizard implementation.
     */
    private String path;

    private boolean wizardRequiresLazyInitialisation = false;

    /**
     * Setter for the wizard identifier.
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
     * on a form. Without this, the first submit button would always be the one used.
     */
    private String submit;

    private ObjectFactory objectFactory;

    private ValidationManager validationManager;

    private ConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;

    /**
     * Required resource.
     *
     * @param objectFactory instance
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Setter for the cancel field.
     *
     * @param cancel
     */
    public void setCancel(String cancel)
    {
        this.cancel = cancel;
    }

    /**
     * Setter for the next field.
     *
     * @param next
     */
    public void setNext(String next)
    {
        this.next = next;
    }

    /**
     * Setter for the previous field.
     *
     * @param previous
     */
    public void setPrevious(String previous)
    {
        this.previous = previous;
    }

    /**
     * Setter for the finish field.
     *
     * @param finish
     */
    public void setFinish(String finish)
    {
        this.finish = finish;
    }

    /**
     * Setter for the submit field.
     *
     * @param submit
     */
    public void setSubmit(String submit)
    {
        this.submit = submit;
    }

    public boolean isInitialised()
    {
        // ensure that the wizard instance is available / instantiated.
        getWizardInstance();
        return !wizardRequiresLazyInitialisation;
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

    public boolean isFinishSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("finish");
        }
        else
        {
            return TextUtils.stringSet(finish);
        }
    }

    protected void initWizardIfRequired()
    {
        Wizard wizard = getWizardInstance();
        if (wizardRequiresLazyInitialisation)
        {
            wizard.initialise();
            wizardRequiresLazyInitialisation = false;
        }
    }

    private boolean validateState()
    {
        // popupate the state, extract the type details.

        try
        {
            CompositeType type = getState().type();
            type.populateInstance(ActionContext.getContext().getParameters(), getState().data());
        }
        catch (TypeException e)
        {
            e.printStackTrace(); 
        }
/*
        try
        {
            Object state = getState();
            FormSupport support = createFormSupport(state);
            ValidationContext validationContext = createValidationContext(state, this);

            support.populateObject(state);
            validationManager.validate(state, validationContext);

            return !hasErrors();
        }
        catch (ValidationException e)
        {
            addActionError(e.getMessage());
            return false;
        }
*/
        return true;
    }

    private boolean validateWizard()
    {
/*
        try
        {
            Object wizard = getWizardInstance();

            ValidationContext validationContext = createValidationContext(wizard, this);

            // validate the form input
            validationManager.validate(wizard, validationContext);

            return !hasErrors();
        }
        catch (ValidationException e)
        {
            addActionError(e.getMessage());
            return false;
        }
*/
        return true;
    }

    public String execute()
    {
        // validation.
        if (isNextSelected() || isFinishSelected())
        {
            if (!validateState() || !validateWizard())
            {
                return "step";
            }
        }

        try
        {
            initWizardIfRequired();

            if (isCancelSelected())
            {
                return doCancel();
            }
            if (isNextSelected())
            {
                return doNext();
            }
            if (isPreviousSelected())
            {
                return doPrevious();
            }
            if (isFinishSelected())
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
        try
        {
            initWizardIfRequired();

            getWizardInstance().doFinish();

            removeWizard();

            return SUCCESS;
        }
        catch (Exception e)
        {
            handleException(e);
            return ERROR;
        }
    }

    private String doPrevious()
    {
        try
        {
            initWizardIfRequired();

            getWizardInstance().doPrevious();

            return "step";
        }
        catch (Exception e)
        {
            handleException(e);
            return ERROR;
        }
    }

    private String doNext()
    {
        try
        {
            initWizardIfRequired();

            getWizardInstance().doNext();

            return "step";
        }
        catch (Exception e)
        {
            handleException(e);
            return ERROR;
        }
    }

    private String doCancel()
    {
        try
        {
            initWizardIfRequired();

            getWizardInstance().doCancel();
            removeWizard();

            return "cancel";
        }
        catch (Exception e)
        {
            handleException(e);
            return ERROR;
        }
    }

    public WizardState getState()
    {
        return getWizardInstance().getCurrentState();
    }

    private void removeWizard()
    {
        ActionContext.getContext().getSession().remove(path);
    }

    private void handleException(Exception e)
    {
        LOG.error(e.getMessage(), e);
        addActionError("Unexpected exception: " + e.getClass().getName() + ", " + e.getMessage());

        // remove the wizard from the session so that we can start fresh
        removeWizard();
    }

    public Wizard getWizardInstance()
    {
        try
        {
            Map<String, Object> session = ActionContext.getContext().getSession();
            if (!session.containsKey(path))
            {
                DefaultTypeWizard wizardInstance = new DefaultTypeWizard(path);
                ComponentContext.autowire(wizardInstance);
                wizardRequiresLazyInitialisation = true;
                session.put(path, wizardInstance);
            }
            return (Wizard) session.get(path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
