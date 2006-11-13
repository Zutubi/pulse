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

    private Object state;

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

    protected boolean isSubmitted()
    {
        return isCancelSelected() || isSaveSelected() || isResetSelected();
    }

    protected FormSupport createFormSupport(Object subject)
    {
        FormSupport support = new FormSupport();
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

    public Object getState()
    {
        return state;
    }

    public String execute() throws Exception
    {
        state = doLoad();

        if (!isSubmitted())
        {
            // render the form.
            return "input";
        }

        if (isCancelSelected())
        {
            return "cancel";
        }

        if (isResetSelected())
        {
            return "input";
        }

        // populate the state from the posted data.

        MessagesTextProvider textProvider = new MessagesTextProvider(state);
        ValidationContext ctx = new DelegatingValidationContext(new XWorkValidationAdapter(this), textProvider);

        FormSupport formsupport = createFormSupport(state);
        formsupport.populateObject(state);
        validationManager.validate(state, ctx);

        if (hasErrors())
        {
            return "input";
        }

        // validate this.
        validate(ctx);

        if (hasErrors())
        {
            return "input";
        }

        // persist change.
        doSave(state);

        return SUCCESS;
    }

    public abstract Object doLoad();

    public abstract void doSave(Object obj);
}
