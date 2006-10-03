package com.zutubi.pulse.form.ui;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.validator.ValidatorContext;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.ActionDescriptor;
import com.zutubi.pulse.form.persistence.Copyable;
import com.zutubi.pulse.form.persistence.ObjectStore;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.ui.components.Form;
import com.zutubi.pulse.form.ui.components.UIComponent;
import com.zutubi.pulse.form.ui.renderers.FreemarkerTemplateRenderer;
import com.zutubi.validation.*;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;

import java.util.Map;
import java.io.StringWriter;

import freemarker.template.Configuration;

/**
 * <class-comment/>
 */
public class FormAction extends ActionSupport
{
    /**
     *
     */
    private static final String CANCEL = "cancel";

    /**
     *
     */
    private static final String SAVE = "save";

    /**
     *
     */
    private static final String RESET = "reset";

    /**
     *
     */
    private String objectKey;

    private ValidationContext validatorContext = new DelegatingValidationContext(this);

    /**
     *
     */
    private ObjectStore objectStore;

    private String renderedForm;

    private DescriptorFactory descriptorFactory;

    private ValidationManager validationManager;

    private Configuration configuration;

    public void setObjectKey(String objectKey)
    {
        this.objectKey = objectKey;
    }

    public String getRenderedForm()
    {
        return renderedForm;
    }

    public String execute() throws Exception
    {

        Map parameters = ActionContext.getContext().getParameters();
        if (parameters.containsKey(ActionDescriptor.CANCEL))
        {
            return doCancel();
        }
        else if (parameters.containsKey(ActionDescriptor.RESET))
        {
            return doReset();
        }
        else if (parameters.containsKey(ActionDescriptor.SAVE))
        {
            return doSave();
        }
        else
        {
            return doInput();
        }
    }

    public String doSave() throws Exception
    {
        Copyable obj = objectStore.load(objectKey);

        // copy the form values into the object, checking for conversion errors.
        FormSupport support = new FormSupport();
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setValidationManager(validationManager);
        support.validate(obj, validatorContext);

        if (validatorContext.hasErrors())
        {
            // prepare for rendering.
            doRender(obj, validatorContext);
            return INPUT;
        }

        // persist the changes.
        objectStore.save(objectKey, obj);

        // do rendering...
        doRender(obj, null);

        return SAVE;
    }

    public String doReset() throws Exception
    {
        Copyable obj = objectStore.reset(objectKey);

        doRender(obj, null);

        return RESET;
    }

    public String doCancel() throws Exception
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj, null);

        return CANCEL;
    }

    public String doInput() throws Exception
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj, null);

        return INPUT;
    }

    private void doRender(Object obj, ValidationContext context) throws Exception
    {
        FormSupport support = new FormSupport();
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setValidationManager(validationManager);

        renderedForm = support.renderForm(obj, context);
    }

    /**
     * Required resource.
     *
     * @param objectStore
     */
    public void setObjectStore(ObjectStore objectStore)
    {
        this.objectStore = objectStore;
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

    public void setFreemarkerConfiguration(Configuration config)
    {
        this.configuration = config;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
