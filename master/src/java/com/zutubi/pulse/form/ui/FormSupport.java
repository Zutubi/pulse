package com.zutubi.pulse.form.ui;

import freemarker.template.Configuration;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.ui.components.Form;
import com.zutubi.pulse.form.ui.components.UIComponent;
import com.zutubi.pulse.form.ui.renderers.FreemarkerTemplateRenderer;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.TextProvider;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationManager;
import com.zutubi.validation.ValidationException;
import com.opensymphony.xwork.ActionContext;

import java.io.StringWriter;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FormSupport
{
    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    private ValidationManager validationManager;

    public void setTextProvider(TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }

    private TextProvider textProvider;

    public void validate(Object obj, ValidationContext validatorContext) throws ValidationException
    {
        populateObject(obj, validatorContext);

        // validate the form input
        validationManager.validate(obj, validatorContext);
    }

    public String renderForm(Object obj, ValidationContext context) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());

        return renderDescriptor(descriptor, obj, context);
    }

    public String renderWizard(Object obj, String state, ValidationContext context, boolean isFirstState, boolean isLastState) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());
        WizardDecorator decorator = new WizardDecorator();
        decorator.setState(state);
        decorator.setFirstState(isFirstState);
        decorator.setLastState(isLastState);
        descriptor = decorator.decorate(descriptor);

        return renderDescriptor(descriptor, obj, context);
    }

    private String renderDescriptor(FormDescriptor descriptor, Object obj, ValidationContext context)
            throws Exception
    {
        PropertiesFormDecorator decorator = new PropertiesFormDecorator(textProvider);
        descriptor = decorator.decorate(descriptor);

        // build the form.
        Form form = new FormFactory().createForm(descriptor, obj);
        populateForm(form, descriptor, obj);

        // setup the rendering resources.
        StringWriter writer = new StringWriter();
        FreemarkerTemplateRenderer templateRenderer = new FreemarkerTemplateRenderer();
        templateRenderer.setConfiguration(configuration);
        templateRenderer.setWriter(writer);
        templateRenderer.setValidationContext(context);

        ComponentRenderer renderer = new ComponentRenderer();
        renderer.setTemplateRenderer(templateRenderer);
        renderer.setTextProvider(textProvider);

        // render it.
        renderer.render(form);

        return writer.toString();
    }

    private void populateForm(Form form, FormDescriptor formDescriptor, Object obj)
    {
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
                //e.printStackTrace();
            }
            catch (SqueezeException e)
            {
                e.printStackTrace();
            }
        }
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

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
