package com.zutubi.pulse.master.tove.velocity;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.model.Table;
import com.zutubi.pulse.master.tove.table.TableDescriptor;
import com.zutubi.pulse.master.tove.table.TableDescriptorFactory;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.mortbay.http.EOFException;

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

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node)
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

            Template template = configuration.getTemplate("tove/xhtml/table.ftl");
            template.process(context, writer);
            return true;
        }
        catch (EOFException e)
        {
            // Client end probably closed the connection, don't clutter logs.
            return true;
        }
        catch (Throwable throwable)
        {
            LOG.warning(throwable);
            try
            {
                writer.write(renderError("Failed to render table. Unexpected " + throwable.getClass() + ": " + throwable.getMessage()));
            }
            catch (IOException e)
            {
                // We did our best.
            }

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
