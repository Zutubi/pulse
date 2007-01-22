package com.zutubi.pulse.form.ui;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.form.TextProvider;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.ui.components.Component;
import com.zutubi.pulse.form.ui.components.Form;
import com.zutubi.pulse.form.ui.renderers.FreemarkerTemplateRenderer;
import com.zutubi.pulse.prototype.TemplateFormDecorator;
import com.zutubi.pulse.prototype.TemplateRecord;
import com.zutubi.pulse.wizard.Wizard;
import freemarker.template.Configuration;
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlException;

import java.io.StringWriter;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FormSupport
{
    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    private TextProvider textProvider;

    private String theme;

    public String renderForm(Object obj) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());

        return renderDescriptor(descriptor, obj);
    }

    public String renderForm(Class clazz, TemplateRecord record) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(clazz);

        TemplateFormDecorator decorator = new TemplateFormDecorator(record);
        descriptor = decorator.decorate(descriptor);

        return renderDescriptor(descriptor, record);
    }

    public String renderWizard(Wizard wizard, Object obj) throws Exception
    {
        FormDescriptor descriptor = descriptorFactory.createFormDescriptor(obj.getClass());

        WizardDecorator decorator = new WizardDecorator(wizard);
        descriptor = decorator.decorate(descriptor);

        return renderDescriptor(descriptor, obj);
    }

    private String renderDescriptor(FormDescriptor descriptor, Object obj)
            throws Exception
    {
        PropertiesFormDecorator decorator = new PropertiesFormDecorator(textProvider);
        descriptor = decorator.decorate(descriptor);

        // build the form.
        Form form = new FormFactory().createForm(descriptor, obj);
        return render(form, obj);
    }

    public void populateObject(Object obj)
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
                    Ognl.setValue(name, obj, value);
                }
                catch (NoSuchPropertyException e)
                {
                    // ignore.
                }
                catch (OgnlException e)
                {
                    e.printStackTrace();
                }
                catch (SqueezeException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public String render(Component component, Object obj) throws Exception {

        // setup the rendering resources.
        StringWriter writer = new StringWriter();
        FreemarkerTemplateRenderer templateRenderer = new FreemarkerTemplateRenderer();
        templateRenderer.setConfiguration(configuration);
        if (TextUtils.stringSet(theme))
        {
            templateRenderer.setTheme(theme);
        }

        RenderContext context = new RenderContext(templateRenderer, textProvider);
        context.setWriter(writer);
//        ComponentRenderer renderer = new ComponentRenderer();
//        renderer.setContext(context);

        // we are cheating a bit here but using the ActionContext value stack to manipulate the
        // subsequent renderer context. We should be a little more direct in how we provide the
        // objects details to the RenderContext (and subsequent retrieval via get(key);
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        try
        {
            stack.push(obj);

            // render it.
            component.render(context, templateRenderer);
//            renderer.render(component);
        }
        finally
        {
            stack.pop();
        }
        writer.flush();

        return writer.toString();
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

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setTextProvider(TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}
