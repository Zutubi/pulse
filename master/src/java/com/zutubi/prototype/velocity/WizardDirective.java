package com.zutubi.prototype.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.WizardDescriptor;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.model.HiddenFieldDescriptor;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class WizardDirective extends AbstractDirective
{
    private String path;
    private boolean decorate = true;

    private FormDescriptorFactory formDescriptorFactory;

    private Configuration configuration;
    private Wizard wizardInstance;

    public WizardDirective()
    {
        ComponentContext.autowire(this);
    }

    public String getName()
    {
        return "pwizard";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        String sessionKey = PathUtils.normalizePath(path);
        wizardInstance = (Wizard) ActionContext.getContext().getSession().get(sessionKey);
        if (wizardInstance == null)
        {
            return false;
        }

        WizardState currentState = wizardInstance.getCurrentState();
        if (currentState == null)
        {
            return false;
        }

        writer.write(internalRender(currentState));

        return true;
    }

    private String internalRender(WizardState state) throws IOException, ParseErrorException
    {
        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages stateMessages = state.getMessages();
            Messages wizardMessages = Messages.getInstance(wizardInstance.getClass());

            WizardDescriptor wizardDescriptor = new WizardDescriptor(wizardInstance);
            wizardDescriptor.setFormDescriptorFactory(formDescriptorFactory);
            wizardDescriptor.setDecorate(decorate);

            Map<String, Object> context = new HashMap<String, Object>();

            context.put("i18nText", new GetTextMethod(stateMessages, wizardMessages));

            context.put("wizard", wizardDescriptor.instantiate(path, state.getRecord()));

            // validation support:
            OgnlValueStack stack = ActionContext.getContext().getValueStack();
            context.put("fieldErrors", stack.findValue("fieldErrors"));

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            // provide wizard specific rendering, that includes details about all of the steps, the current step
            // index, and much much more.
            Template template = configuration.getTemplate("prototype/xhtml/wizard.ftl");
            template.process(context, writer);

            return writer.toString();
        }
        catch (TemplateException e)
        {
            throw new ParseErrorException(e.getMessage());
        }
    }

    private void decorate(FormDescriptor formDescriptor)
    {
        List<String> actions = CollectionUtils.map(wizardInstance.getAvailableActions(), new Mapping<WizardTransition, String>()
        {
            public String map(WizardTransition o)
            {
                return o.name().toLowerCase();
            }
        });
        formDescriptor.setActions(actions);

        HiddenFieldDescriptor hiddenStateField = new HiddenFieldDescriptor();
        hiddenStateField.setName("state");
        hiddenStateField.setValue(wizardInstance.getCurrentStateIndex());

        formDescriptor.add(hiddenStateField);
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setAction(String action)
    {
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setDecorate(boolean decorate)
    {
        this.decorate = decorate;
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }
}
