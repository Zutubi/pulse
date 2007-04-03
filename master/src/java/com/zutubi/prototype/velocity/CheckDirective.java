package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.annotation.ConfigurationCheck;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class CheckDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(CheckDirective.class);

    private String action;

    private FormDescriptorFactory formDescriptorFactory;

    private TypeRegistry typeRegistry;

    private Configuration configuration;

    /**
     * The name of this velocity directive.
     *
     * @return name
     */
    public String getName()
    {
        return "checkform";
    }

    public int getType()
    {
        return LINE;
    }

    /**
     * The generated forms action attribute.
     *
     * @param action attribute
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(contextAdapter, node);
            wireParams(params);

            Type type = lookupType();

            CompositeType ctype = (CompositeType) type;

            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(ctype.getSymbolicName());

            // decorate the form to include the symbolic name as a hidden field. This is necessary for
            // configuration. This is probably not the best place for this, but until i think of a better location,
            // here it stays.
            FieldDescriptor hiddenFieldDescriptor = new FieldDescriptor();
            hiddenFieldDescriptor.setName("symbolicName");
            hiddenFieldDescriptor.addParameter("value", ctype.getSymbolicName());
            hiddenFieldDescriptor.addParameter("type", "hidden");
            formDescriptor.add(hiddenFieldDescriptor);

            for (FieldDescriptor fd : formDescriptor.getFieldDescriptors())
            {
                fd.setType("hidden");
            }

            List<String> originalFieldNames = new LinkedList<String>();
            for (FieldDescriptor fd : formDescriptor.getFieldDescriptors())
            {
                // problem: by changing the field names, any annotations (fieldOrder in particular) that references
                // fields by name will fail.
                originalFieldNames.add(fd.getName());
                fd.setName(fd.getName() + "_check");
            }
            formDescriptor.addParameter("originalFields", originalFieldNames);

            // lookup and construct the configuration test form.
            ConfigurationCheck annotation = (ConfigurationCheck) ctype.getAnnotation(ConfigurationCheck.class);
            Class checkClass = annotation.value();
            CompositeType checkType = typeRegistry.getType(checkClass);

            FormDescriptor checkFormDescriptor = formDescriptorFactory.createDescriptor(checkType);
            for (FieldDescriptor fd : checkFormDescriptor.getFieldDescriptors())
            {
                formDescriptor.add(fd);
            }
            formDescriptor.setActions(Arrays.asList("check"));

            Map<String, Object> context = initialiseContext(checkType.getClazz());

            Form form = formDescriptor.instantiate(null);
            form.setAction(action);
            context.put("form", form);

            try
            {
                Template template = configuration.getTemplate("prototype/xhtml/test-form.ftl");
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

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
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
}
