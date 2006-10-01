package com.zutubi.pulse.web.wizard;

import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.validation.*;
import freemarker.template.Configuration;

/**
 * <class-comment/>
 */
public class FormWizardState extends BaseWizardState implements Validateable
{
    /**
     * The validation manager is used to handle the state validation.
     */
    private ValidationManager validationManager;

    /**
     * The descriptor factory is required to generate the form descriptor
     */
    private DescriptorFactory descriptorFactory;

    /**
     * The freemarker configuration is required for rendering the form.
     */
    private Configuration configuration;

    private String renderedForm;

    /**
     * The subject defines the object being used to generate the form.
     */
    private Object subject;

    private String nextStateName;

    private boolean firstState;
    private boolean lastState;

    public FormWizardState(Wizard wizard, Object obj, String stateName, String nextStateName, boolean isFirstState, boolean isLastState)
    {
        super(wizard, stateName);

        this.subject = obj;
        this.nextStateName = nextStateName;
        this.firstState = isFirstState;
        this.lastState = isLastState;
    }

    public Object getSubject()
    {
        return subject;
    }

    public String getNextStateName()
    {
        return nextStateName;
    }

    public void initialise()
    {
        super.initialise();

        render();
    }

    private FormSupport createFormSupport()
    {
        FormSupport support = new FormSupport();
        support.setValidationManager(validationManager);
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));
        return support;
    }

    public String getForm()
    {
        return renderedForm;
    }

    public String getView()
    {
        return "form";
    }

    public void validate()
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        ValidationContext validatorContext = new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);

        FormSupport support = createFormSupport();

        try
        {
            support.validate(subject, validatorContext);
        }
        catch (ValidationException e)
        {
            validatorContext.addActionError(e.getMessage());
        }

        render();
    }

    public void render()
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        ValidationContext validatorContext = new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);

        try
        {
            renderedForm = createFormSupport().renderWizard(subject, getStateName(), validatorContext, firstState, lastState);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Required resource.
     *
     * @param validationManager
     */
    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    /**
     * Required resource.
     *
     * @param descriptorFactory
     */
    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * Required resource.
     *
     * @param configuration
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
