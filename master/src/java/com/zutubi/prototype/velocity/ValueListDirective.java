package com.zutubi.prototype.velocity;

import com.zutubi.prototype.Path;
import com.zutubi.prototype.TableDescriptor;
import com.zutubi.prototype.TableDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.prototype.record.Record;
import com.opensymphony.util.TextUtils;
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
    public String getName()
    {
        return "plist";
    }

    public int getType()
    {
        return LINE;
    }

    private String propertyName;

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        Map params = createPropertyMap(context, node);
        wireParams(params);

        Record parent = projectConfigurationManager.getRecord(path);

        String symbolicName = parent.getSymbolicName();
        if (!TextUtils.stringSet(symbolicName))
        {
            symbolicName = projectConfigurationManager.getSymbolicName(path);
        }

        Record record = projectConfigurationManager.getRecord(new Path(path, propertyName));

        // render the form.
        writer.write(internalRender(symbolicName, record));

        return true;
    }

    private String internalRender(String symbolicName, Record subject) throws IOException, ParseErrorException
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
            context.put("path", new Path(path, propertyName).toString());

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
