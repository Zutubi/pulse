package com.zutubi.prototype.velocity;

import com.zutubi.prototype.TableDescriptor;
import com.zutubi.prototype.TableDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.prototype.TemplateRecord;
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
public class ValueListDirective extends PrototypeDirective
{
    private String propertyName;

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
        Map params = createPropertyMap(context, node);
        wireParams(params);

        long projectId = Long.valueOf(scope.substring(8));

        String symbolicName = projectConfigurationManager.getSymbolicName(path);
        TemplateRecord record = projectConfigurationManager.getRecord(projectId, path);

        // render the form.
        writer.write(internalRender(symbolicName, propertyName, record.get(propertyName)));

        return true;
    }

    private String internalRender(String symbolicName, String propertyName, Object subject) throws IOException, ParseErrorException
    {
        TableDescriptorFactory tableFactory = new TableDescriptorFactory();
        tableFactory.setTypeRegistry(recordTypeRegistry);
        TableDescriptor tableDescriptor = tableFactory.createDescriptor(symbolicName, propertyName);

        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages messages = Messages.getInstance(recordTypeRegistry.getType(symbolicName));

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("table", tableDescriptor.instantiate(subject));
            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);
            context.put("scope", scope);

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

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
}
