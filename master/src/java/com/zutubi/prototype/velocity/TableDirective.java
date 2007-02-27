package com.zutubi.prototype.velocity;

import com.zutubi.prototype.TableDescriptor;
import com.zutubi.prototype.TableDescriptorFactory;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.logging.Logger;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.exception.ParseErrorException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class TableDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(TableDirective.class);

    public String getName()
    {
        return "plist";
    }

    public int getType()
    {
        return LINE;
    }

    public String doRender(Type collectionType) throws IOException, ParseErrorException, TypeException
    {
        Record record = configurationPersistenceManager.getRecord(path);

        TableDescriptorFactory tableFactory = new TableDescriptorFactory();
        tableFactory.setTypeRegistry(typeRegistry);
        TableDescriptor tableDescriptor = tableFactory.createTableDescriptor((CollectionType)collectionType);

        Type type = ((CollectionType)collectionType).getCollectionType();
        
        // handle rendering of the freemarker template.
        StringWriter writer = new StringWriter();

        try
        {
            Messages messages = Messages.getInstance(type.getClazz());

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("table", tableDescriptor.instantiate(record));
            context.put("i18nText", new GetTextMethod(messages));
            context.put("path", path);

            DelegateBuiltin.conditionalRegistration("i18n", "i18nText");

            Template template = configuration.getTemplate("prototype/table.ftl");
            template.process(context, writer);
            return writer.toString();
        }
        catch (TemplateException e)
        {
            LOG.warning(e);
            throw new ParseErrorException(e.getMessage());
        }
    }
}
