package com.zutubi.prototype.velocity;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.i18n.Messages;
import freemarker.template.TemplateException;
import freemarker.template.Template;
import freemarker.core.DelegateBuiltin;

public class FormDirective extends PrototypeDirective
{
    public String getName()
    {
        return "pform";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        String symbolicName = lookupSymbolicName();
        Record record = recordManager.load(path.toString());

        if (record != null)
        {
            writer.write(internalRender(symbolicName, record));
        }
        else
        {
            writer.write(internalRender(symbolicName, null));
        }

        return true;
    }

    private String internalRender(String symbolicName, Record subject) throws IOException, ParseErrorException
    {
        FormDescriptorFactory formFactory = new FormDescriptorFactory();
        formFactory.setTypeRegistry(typeRegistry);
        FormDescriptor formDescriptor = formFactory.createDescriptor(symbolicName);

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages messages = Messages.getInstance(typeRegistry.getType(symbolicName));

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("form", formDescriptor.instantiate(subject));
            context.put("i18nText", new GetTextMethod(messages));
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

}
