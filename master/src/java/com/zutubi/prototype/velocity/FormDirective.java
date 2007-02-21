package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeConversionException;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.webwork.Configuration;
import com.zutubi.pulse.i18n.Messages;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FormDirective extends PrototypeDirective
{
    private String action;

    public String getName()
    {
        return "pform";
    }

    public int getType()
    {
        return LINE;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Configuration configuration = new Configuration(path);
            configuration.analyse();

            String symbolicName = configuration.getTypeSymbolicName();

            Object data = configurationPersistenceManager.getInstance(path);

            writer.write(internalRender(symbolicName, data));

            return true;
        }
        catch (TypeException e)
        {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private String internalRender(String symbolicName, Object data) throws IOException, ParseErrorException, TypeConversionException
    {
        FormDescriptorFactory formFactory = new FormDescriptorFactory();
        formFactory.setTypeRegistry(typeRegistry);
        FormDescriptor formDescriptor = formFactory.createDescriptor(symbolicName);

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        Type type = typeRegistry.getType(symbolicName);

        try
        {
            Messages messages = Messages.getInstance(type.getClazz());
            Form form = formDescriptor.instantiate(data);
            form.setAction(action);

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("form", form);
            context.put("i18nText", new GetTextMethod(messages));

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
