package com.zutubi.pulse.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.ui.FormSupport;
import freemarker.template.Configuration;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * <class comment/>
 */
public class FormDirective extends AbstractDirective
{
    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    private String object;

    private String theme;

    public FormDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "form";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        // render the form.
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        Object instance = stack.findValue(object);
        writer.write(internalRender(instance));
        return true;
    }

    public void setObject(String name)
    {
        this.object = name;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    private String internalRender(Object subject)
    {
        FormSupport support = createFormSupport(subject);

        try
        {
            // rendering should be much simpler once the state, first and last variables are removed.
            return support.renderForm(subject);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private FormSupport createFormSupport(Object subject)
    {
        FormSupport support = new FormSupport();
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));
        support.setTheme(theme);
        return support;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }


}
