package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.util.logging.Logger;
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
public class SimpleFormDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(SimpleFormDirective.class);

    private Configuration configuration;

    /**
     * The name of this velocity directive.
     *
     * @return name
     */
    public String getName()
    {
        return "sform";
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

            Record data = lookupRecord();

            // Create the context object used to define the freemarker rendering context
            Map<String, Object> context = initialiseContext((Class) lookup("class"));

            FormDescriptor formDescriptor = (FormDescriptor) lookup("descriptor");
            context.put("form", formDescriptor.instantiate("transient", data));

            try
            {
                Template template = configuration.getTemplate("prototype/xhtml/form.ftl");
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
