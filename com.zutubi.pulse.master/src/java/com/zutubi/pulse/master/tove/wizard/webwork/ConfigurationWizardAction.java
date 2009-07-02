package com.zutubi.pulse.master.tove.wizard.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.wizard.AbstractTypeWizard;
import com.zutubi.pulse.master.tove.wizard.SingleTypeWizard;
import com.zutubi.pulse.master.tove.wizard.Wizard;
import com.zutubi.pulse.master.tove.wizard.WizardState;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.xwork.XWorkValidationAdapter;

import java.util.Map;

/**
 *
 *
 */
public class ConfigurationWizardAction extends com.opensymphony.xwork.ActionSupport
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

    private ObjectFactory objectFactory;

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
        Wizard wizard = getWizardInstance();
        return !wizardRequiresLazyInitialisation && wizard.getCurrentState() != null;
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
                return state.validate(this);
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
        if(getWizardInstance().doNext() != null)
        {
            return "step";
        }
        else
        {
            // The next state has no form to show, so head straight for the
            // finish line.
            return doFinish();
        }
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
            if (wizardRequiresLazyInitialisation || wizard.getCurrentState() == null)
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
        ActionContext.getContext().getSession().remove(getSessionKey(this.path));
    }

    @SuppressWarnings({ "unchecked" })
    public Wizard getWizardInstance()
    {
        try
        {
            Wizard wizard = getWizardInstance(this.path);
            if (wizard == null)
            {
                wizard = doCreateWizard();
                ActionContext.getContext().getSession().put(getSessionKey(this.path), wizard);
            }
            return wizard;
        }
        catch (Exception e)
        {
            LOG.warning(e);
            return null;
        }
    }

    public static String getSessionKey(String path)
    {
        return PathUtils.normalisePath(path);
    }

    public static Wizard getWizardInstance(String path)
    {
        Map session = ActionContext.getContext().getSession();
        return (Wizard) session.get(getSessionKey(path));
    }

    @SuppressWarnings({ "unchecked" })
    protected Wizard doCreateWizard()
    {
        AbstractTypeWizard wizardInstance = null;

        String parentPath = PathUtils.getParentPath(path);
        Type type;
        String insertPath;
        boolean template;
        String templateParentPath = null;
        TemplateRecord templateParentRecord = null;

        // The incoming path for a templated scope should hold the template
        // parent as its last element.
        if(parentPath != null && configurationTemplateManager.isTemplatedCollection(parentPath))
        {
            type = configurationTemplateManager.getType(parentPath).getTargetType();
            templateParentPath = path;
            templateParentRecord = (TemplateRecord) configurationTemplateManager.getRecord(templateParentPath);
            if (templateParentRecord == null)
            {
                throw new IllegalArgumentException("Invalid wizard path '" + path + "': template parent does not exist");
            }

            insertPath = parentPath;
            template = isSelected(CREATE_TEMPLATE);
        }
        else
        {
            type = configurationTemplateManager.getType(path);
            insertPath = path;

            if (type instanceof CollectionType)
            {
                type = ((CollectionType) type).getCollectionType();
                parentPath = path;
            }

            String templateOwner = configurationTemplateManager.getTemplateOwnerPath(path);
            template = templateOwner != null && !configurationTemplateManager.getTemplateNode(templateOwner).isConcrete();
        }

        Class wizardClass = ConventionSupport.getWizard(type);
        if (wizardClass != null)
        {
            wizardInstance = (AbstractTypeWizard) objectFactory.buildBean(wizardClass);
        }

        if (wizardInstance == null)
        {
            wizardInstance = objectFactory.buildBean(SingleTypeWizard.class);
        }

        wizardInstance.setParameters(parentPath, insertPath, templateParentPath, templateParentRecord, template);

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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
