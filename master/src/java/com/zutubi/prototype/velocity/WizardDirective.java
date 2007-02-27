package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.wizard.WizardTransition;
import com.zutubi.prototype.wizard.Wizard;
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
    private String action;
    private String path;

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

        String sessionKey = normalizePath(path);
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

    private String normalizePath(String path)
    {
        if (TextUtils.stringSet(path))
        {
            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }
            if (path.endsWith("/"))
            {
                path = path.substring(0, path.length() -1);
            }
        }
        return path;
    }

    private String internalRender(WizardState state) throws IOException, ParseErrorException
    {
        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages messages = Messages.getInstance(state.getType().getClazz());

            Type type = state.getType();
            
            // generate the form.
            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(type.getSymbolicName());

            // need to decorate the form a little bit to handle the fact that it is being rendered as a wizard.
            decorate(formDescriptor);

            Map<String, Object> context = new HashMap<String, Object>();

            Form form = formDescriptor.instantiate(state.getRecord());
            form.setAction(action);
            
            context.put("form", form);
            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);

            
            com.zutubi.prototype.model.Wizard wizard = new com.zutubi.prototype.model.Wizard();
            wizard.setStepCount(wizardInstance.getStateCount());
            wizard.setCurrentStep(wizardInstance.getCurrentStateIndex() + 1);
            context.put("wizard", wizard);

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
        hiddenStateField.addParameter("value", wizardInstance.getCurrentState().getClass().toString());

        formDescriptor.add(hiddenStateField);
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }
}
