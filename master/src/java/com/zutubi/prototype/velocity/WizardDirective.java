package com.zutubi.prototype.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;
import com.zutubi.pulse.velocity.AbstractDirective;
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

            // generate the form.
            FormDescriptor formDescriptor = state.createFormDescriptor(formDescriptorFactory, path);
            formDescriptor.setAction("wizard");

            // need to decorate the form a little bit to handle the fact that it is being rendered as a wizard.
            decorate(formDescriptor);

            Map<String, Object> context = new HashMap<String, Object>();

            // need to handle the template record here.
            // a) we have a data map that contains posted information.
            // b) we have a template record that contains default data.
            // maybe the data map needs to be merged first, external to this

            Form form = formDescriptor.instantiate(path, state.getRecord());

            context.put("form", form);
            context.put("i18nText", new GetTextMethod(stateMessages, wizardMessages));
            context.put("path", path);
            context.put("decorate", decorate);

            com.zutubi.prototype.model.Wizard wizard = new com.zutubi.prototype.model.Wizard();
            wizard.setStepCount(wizardInstance.getStateCount());
            wizard.setCurrentStep(wizardInstance.getCurrentStateIndex() + 1);
            context.put("wizard", wizard);

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

        FieldDescriptor hiddenStateField = new FieldDescriptor();
        hiddenStateField.setType("hidden");
        hiddenStateField.setName("state");
        hiddenStateField.addParameter("value", wizardInstance.getCurrentStateIndex());

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
