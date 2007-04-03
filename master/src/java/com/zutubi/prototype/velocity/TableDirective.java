package com.zutubi.prototype.velocity;

import com.zutubi.prototype.TableDescriptor;
import com.zutubi.prototype.TableDescriptorFactory;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 *
 */
public class TableDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(TableDirective.class);
    private Configuration configuration;

    public String getName()
    {
        return "plist";
    }

    public int getType()
    {
        return LINE;
    }

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(contextAdapter, node);
            wireParams(params);

            Type collectionType = lookupType();

            Record record = lookupRecord();

            TableDescriptorFactory tableFactory = new TableDescriptorFactory();
            ComponentContext.autowire(tableFactory);

            TableDescriptor tableDescriptor = tableFactory.createTableDescriptor((CollectionType)collectionType);

            Type type = ((CollectionType)collectionType).getCollectionType();

            // handle rendering of the freemarker template.

            Map<String, Object> context = initialiseContext(type.getClazz());
            context.put("table", tableDescriptor.instantiate(record));
            context.put("path", lookupPath());
            
            try
            {
                Template template = configuration.getTemplate("prototype/table.ftl");
                template.process(context, writer);
            }
            catch (TemplateException e)
            {
                LOG.warning(e);
                throw new ParseErrorException(e.getMessage());
            }

            return true;
        }
        catch (Exception e)
        {
            LOG.warning(e);
            writer.write(renderError("Failed to render form. Unexpected " + e.getClass() + ": " + e.getMessage()));
            return true;
        }
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
