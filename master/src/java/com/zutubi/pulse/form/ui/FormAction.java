package com.zutubi.pulse.form.ui;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;
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

    private DescriptorFactory descriptorFactory;

    private ValidationManager validationManager;

    private String renderedForm;

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
        populateObject(obj, validatorContext);

        // validate the form input
        validationManager.validate(obj, validatorContext);

        if (validatorContext.hasErrors())
        {
            // prepare for rendering.
            doRender(obj);
            return INPUT;
        }

        // persist the changes.
        objectStore.save(objectKey, obj);

        // do rendering...
        doRender(obj);

        return SAVE;
    }

    public String doReset() throws Exception
    {
        Copyable obj = objectStore.reset(objectKey);

        doRender(obj);

        return RESET;
    }

    public String doCancel() throws Exception
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj);

        return CANCEL;
    }

    public String doInput() throws Exception
    {
        Object obj = objectStore.load(objectKey);

        doRender(obj);

        return INPUT;
    }

    private void doRender(Object obj) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());

        // build the form.
        Form form = new FormFactory().createForm(descriptor, obj);
        populateForm(form, obj);

        // render it.
        StringWriter writer = new StringWriter();
        FreemarkerTemplateRenderer templateRenderer = new FreemarkerTemplateRenderer();
        templateRenderer.setConfiguration(configuration);
        templateRenderer.setWriter(writer);

        ComponentRenderer renderer = new ComponentRenderer();
        renderer.setTemplateRenderer(templateRenderer);
        renderer.render(form);

        renderedForm = writer.toString();
    }

    private void populateObject(Object obj, ValidationContext validatorContext)
    {
        FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
        {
            String name = fieldDescriptor.getName();

            TypeSqueezer squeezer = Squeezers.findSqueezer(fieldDescriptor.getType());

            String[] paramValue = getParameterValue(name);
            if (paramValue != null)
            {
                try
                {
                    Object value = squeezer.unsqueeze(paramValue);
                    BeanUtils.setProperty(name, value, obj);
                }
                catch (SqueezeException e)
                {
                    validatorContext.addFieldError(name, name + ".conversionerror");
                }
                catch (BeanException e)
                {
                    validatorContext.addFieldError(name, name + ".beanerror");
                }
            }
        }
    }

    private String[] getParameterValue(String parameterName)
    {
        Map parameters = ActionContext.getContext().getParameters();
        if (!parameters.containsKey(parameterName))
        {
            return null;
        }
        Object parameterValue = parameters.get(parameterName);
        if (parameterValue instanceof String)
        {
            return new String[]{(String)parameterValue};
        }
        else if (parameterValue instanceof String[])
        {
            return (String[]) parameterValue;
        }

        // unexpected non string type...
        return null;
    }

    private void populateForm(Form form, Object obj)
    {
        FormDescriptor formDescriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        for (FieldDescriptor fieldDescriptor : formDescriptor.getFieldDescriptors())
        {
            try
            {
                String propertyName = fieldDescriptor.getName();
                Object propertyValue = BeanUtils.getProperty(propertyName, obj);

                UIComponent component = (UIComponent) form.getNestedComponent(propertyName);

                TypeSqueezer squeezer = Squeezers.findSqueezer(fieldDescriptor.getType());
                component.setValue(squeezer.squeeze(propertyValue));
            }
            catch (BeanException e)
            {
                e.printStackTrace();
            }
            catch (SqueezeException e)
            {
                e.printStackTrace();
            }
        }
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
