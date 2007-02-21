package com.zutubi.prototype.velocity;

import com.zutubi.prototype.TableDescriptor;
import com.zutubi.prototype.TableDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.type.CollectionType;
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

/**
 *
 *
 */
public class ListDirective extends PrototypeDirective
{
    public String getName()
    {
        return "plist";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            Configuration configuration = new Configuration(path);
            configuration.analyse();

            CollectionType type = (CollectionType)configuration.getType();

            symbolicName = configuration.getTargetSymbolicName();

            Object collection = configurationPersistenceManager.getInstance(path);

            writer.write(internalRender(type, collection));

            return true;
        }
        catch (TypeException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private String internalRender(CollectionType type, Object data) throws IOException, ParseErrorException
    {
        TableDescriptorFactory tableFactory = new TableDescriptorFactory();
        tableFactory.setTypeRegistry(typeRegistry);
        TableDescriptor tableDescriptor = tableFactory.createTableDescriptor(type);

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages messages = Messages.getInstance(typeRegistry.getType(symbolicName).getClazz());

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("table", tableDescriptor.instantiate(data));
            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);

            // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
            DelegateBuiltin.conditionalRegistration("i18n", "i18nText"); //TODO: Move this where it is only run once.

            Template template = configuration.getTemplate("table.ftl");
            template.process(context, writer);
            return writer.toString();
        }
        catch (TemplateException e)
        {
            throw new ParseErrorException(e.getMessage());
        }
    }
}
