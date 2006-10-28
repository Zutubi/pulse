package com.zutubi.pulse.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.ui.FormSupport;
import com.zutubi.pulse.wizard.Wizard;
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
public class WizardDirective extends AbstractDirective
{
    private Configuration configuration;

    private DescriptorFactory descriptorFactory;

    private String object;

    public WizardDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "wizard";
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
        Wizard wizard = (Wizard) stack.findValue(object);

        writer.write(renderState(wizard));

        return true;
    }

    public void setObject(String name)
    {
        this.object = name;
    }

    private String renderState(Wizard wizard)
    {
        Object subject = wizard.getCurrentState();
        FormSupport support = createFormSupport(subject);

        try
        {
            // rendering should be much simpler once the state, first and last variables are removed.
            return support.renderWizard(wizard, subject);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private FormSupport createFormSupport(Object subject)
    {
        FormSupport support = new FormSupport();
        support.setConfiguration(configuration);
        support.setDescriptorFactory(descriptorFactory);
        support.setTextProvider(new com.zutubi.pulse.form.MessagesTextProvider(subject));
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
