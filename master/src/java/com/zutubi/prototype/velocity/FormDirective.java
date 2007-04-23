package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.TemplateFormDecorator;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.HiddenFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
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
public class FormDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(FormDirective.class);

    private FormDescriptorFactory formDescriptorFactory;
    private Configuration configuration;

    /**
     * The name of this velocity directive.
     *
     * @return name
     */
    public String getName()
    {
        return "pform";
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

            Type type = lookupType();

            CompositeType ctype = (CompositeType) type;
            Record data = lookupRecord();

            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(lookupPath(), ctype);

            TemplateFormDecorator templateDecorator = new TemplateFormDecorator(null);
            templateDecorator.decorate(formDescriptor);

            // Decorate the form to include the symbolic name as a hidden field. This is necessary for configuration.
            // This is probably not the best place for this, but until i think of a better location, it stays.
            HiddenFieldDescriptor hiddenFieldDescriptor = new HiddenFieldDescriptor();
            hiddenFieldDescriptor.setName("symbolicName");
            hiddenFieldDescriptor.setValue(ctype.getSymbolicName());
            formDescriptor.add(hiddenFieldDescriptor);

            // Create the context object used to define the freemarker rendering context
            Map<String, Object> context = initialiseContext(type.getClazz());

            Form form = formDescriptor.instantiate(lookupPath(), data);
            context.put("form", form);

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

    /**
     * Required resource
     *
     * @param configuration instance
     */
    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Required resource.
     *
     * @param formDescriptorFactory instance
     */
    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }
}

