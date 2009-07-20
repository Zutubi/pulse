package com.zutubi.pulse.master.tove.velocity;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.pulse.master.tove.model.Table;
import com.zutubi.pulse.master.tove.table.TableDescriptor;
import com.zutubi.pulse.master.tove.table.TableDescriptorFactory;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.Record;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.bean.ObjectFactory;
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
public class TableDirective extends ToveDirective
{
    private ConfigurationTemplateManager configurationTemplateManager;

    private ObjectFactory objectFactory;

    private static final Logger LOG = Logger.getLogger(TableDirective.class);

    private Configuration configuration;

    private String path;

    public TableDirective()
    {
        SpringComponentContext.autowire(this);
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getName()
    {
        return "table";
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

            CollectionType collectionType = (CollectionType) configurationTemplateManager.getType(path);
            
            // lookup the data.
            Record data = configurationTemplateManager.getRecord(path);

            // generate the table descriptor based on the type of the results.
            TableDescriptorFactory tableFactory = objectFactory.buildBean(TableDescriptorFactory.class);

            TableDescriptor tableDescriptor = tableFactory.create(path, collectionType);
            Table table = tableDescriptor.instantiate(path, data);

            // handle rendering of the freemarker template.
            Map<String, Object> context = ToveUtils.initialiseContext(collectionType.getCollectionType().getClazz());
            context.put("table", table);
            context.put("path", path);
            context.put("embedded", ToveUtils.isEmbeddedCollection(collectionType));

            try
            {
                Template template = configuration.getTemplate("tove/xhtml/table.ftl");
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

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
