package com.zutubi.prototype.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.pulse.bootstrap.ComponentContext;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Configuration;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class WizardDirective extends AbstractDirective
{
    private String action;
    private String path;
    
    private Configuration configuration;

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
        Wizard wizardInstance = (Wizard) ActionContext.getContext().getSession().get(sessionKey);
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
            Messages messages = Messages.getInstance(state.getData().getClass());

            Map<String, Object> context = new HashMap<String, Object>();
            Form form = state.getForm(state.getData());
            form.setAction(action);
            
            context.put("form", form);
            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            Template template = configuration.getTemplate("prototype/xhtml/form.ftl");
            template.process(context, writer);

            return writer.toString();
        }
        catch (TemplateException e)
        {
            throw new ParseErrorException(e.getMessage());
        }
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
}
