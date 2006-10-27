package com.zutubi.pulse.web.user.contact;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.validation.MessagesTextProvider;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.validation.*;
import freemarker.template.Configuration;

/**
 * <class comment/>
 */
public abstract class FormAction extends ActionSupport implements Validateable
{
    /**
     * This is set to something if the user has selected the cancel action.
     */
    private String cancel;
    /**
     * This is set to something if the user has selected the save action.
     */
    private String save;
    /**
     * This is set to something if the user has selected the reset action.
     */
    private String reset;

    private String submit;
    protected ValidationManager validationManager;
    private Configuration configuration;
    private DescriptorFactory descriptorFactory;
    protected String renderedForm;

    public void setCancel(String cancel)
    {
        this.cancel = cancel;
    }

    public void setSave(String save)
    {
        this.save = save;
    }

    public void setReset(String reset)
    {
        this.reset = reset;
    }

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

    public boolean isSaveSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("save");
        }
        else
        {
            return TextUtils.stringSet(save);
        }
    }

    public boolean isResetSelected()
    {
        if (TextUtils.stringSet(submit))
        {
            return submit.equals("reset");
        }
        else
        {
            return TextUtils.stringSet(reset);
        }
    }

    public void validate(ValidationContext context)
    {
    }

    public String getRenderedForm()
    {
        return renderedForm;
    }

    protected boolean isSubmitted()
    {
        return isCancelSelected() || isSaveSelected() || isResetSelected();
    }

    protected String renderState(Object subject)
    {

        // Setting up this form support is ugly.  There must be a better way to handle the initialisation
        // of the required objects.
        FormSupport support = createFormSupport(subject);

        ValidationContext validatorContext = createValidationContext(subject, this);
        try
        {
            // rendering should be much simpler once the state, first and last variables are removed.
            return support.renderForm(subject, validatorContext);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private ValidationContext createValidationContext(Object subject, com.opensymphony.xwork.ActionSupport action)
    {
        MessagesTextProvider textProvider = new MessagesTextProvider(subject);
        return new DelegatingValidationContext(new XWorkValidationAdapter(action), textProvider);
    }

    protected FormSupport createFormSupport(Object subject)
    {
        FormSupport support = new FormSupport();
        support.setValidationManager(validationManager);
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));
        return support;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    public String execute() throws Exception
    {
        Object state = doLoad();

        if (!isSubmitted())
        {
            // render the form.
            renderedForm = renderState(state);
            return "input";
        }

        if (isCancelSelected())
        {
            return "cancel";
        }

        if (isResetSelected())
        {
            renderedForm = renderState(state);
            return "input";
        }

        // populate the state from the posted data.

        MessagesTextProvider textProvider = new MessagesTextProvider(state);
        ValidationContext ctx = new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);

        FormSupport formsupport = createFormSupport(state);
        formsupport.populateObject(state, ctx);

        validationManager.validate(state, ctx);

        if (hasErrors())
        {
            renderedForm = renderState(state);
            return "input";
        }

        // validate this.
        validate(ctx);

        if (hasErrors())
        {
            renderedForm = renderState(state);
            return "input";
        }

        // persist change.
        doSave(state);

        return SUCCESS;
    }

    public abstract Object doLoad();

    public abstract void doSave(Object obj);
}
