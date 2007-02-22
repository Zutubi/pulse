package com.zutubi.prototype.wizard.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.prototype.config.ConfigurationCrudSupport;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.XWorkValidationAdapter;

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

    private boolean wizardRequiresLazyInitialisation = false;

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
    private String submit;

    private ObjectFactory objectFactory;

    private ValidationManager validationManager;

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

    /**
     *
     * @return
     */
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

    private boolean validateState()
    {
        // popupate the state, extract the type details.
        try
        {
            Object instance = getState().getData();

            // copy the parameters to the state instance... maybe this should be done separately so that it is
            // not a byproduct of the validation process
            ConfigurationCrudSupport crud = new ConfigurationCrudSupport();
            crud.apply(ActionContext.getContext().getParameters(), instance);
            crud.validate(instance, this);

            return !hasErrors();
        }
        catch (TypeException e)
        {
            addActionError(e.getMessage());
            return false;
        }
    }

    private boolean validateWizard()
    {
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
    }

    private ValidationContext createValidationContext(Object subject, ValidationAware action)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(action), textProvider);
    }

    public String execute()
    {
        // only validate when we are moving forwards in the wizard
        if (isNextSelected() || isFinishSelected())
        {
            if (!validateState() || !validateWizard())
            {
                // if there is a validation failure, then we stay where we are.
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
            else  if (isNextSelected())
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
        removeWizard();
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
            throw (RuntimeException)e;
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
            // normalise the path by stripping leading and trailing '/' chars
            String sessionKey = normalizePath(this.path);
            
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
            e.printStackTrace();
            return null;
        }
    }

    protected Wizard doCreateWizard()
    {
        Wizard wizardInstance;
        if (path.equals("project"))
        {
            wizardInstance = new ConfigureProjectWizard();
        }
        else
        {
            wizardInstance = new ExtendedTypeWizard(path);
        }

        ComponentContext.autowire(wizardInstance);
        wizardRequiresLazyInitialisation = true;        
        return wizardInstance;
    }

    private String normalizePath(String path)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length() -1);
        }
        return path;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
