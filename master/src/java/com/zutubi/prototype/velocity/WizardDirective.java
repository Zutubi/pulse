package com.zutubi.prototype.velocity;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.wizard.Wizard;
import com.zutubi.prototype.wizard.WizardState;
import com.zutubi.prototype.model.Form;
import freemarker.core.DelegateBuiltin;
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
import java.util.Map;

/**
 *
 *
 */
public class WizardDirective extends PrototypeDirective
{
    private String action;

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

        Wizard wizardInstance = (Wizard) ActionContext.getContext().getSession().get(path.toString());

        WizardState currentState = wizardInstance.getCurrentState();

        writer.write(internalRender(currentState));

        return true;
    }

    private String internalRender(WizardState state) throws IOException, ParseErrorException
    {
        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
//            Messages messages = Messages.getInstance(type);

            Map<String, Object> context = new HashMap<String, Object>();
            Form form = state.getForm(null);
            form.setAction(action);
            
            context.put("form", form);
//            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path.toString());

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            Template template = configuration.getTemplate("form.ftl");
            template.process(context, writer);

            return writer.toString();
        }
        catch (TemplateException e)
        {
            throw new ParseErrorException(e.getMessage());
        }
    }

    public void setAction(String action)
    {
        this.action = action;
    }
}
