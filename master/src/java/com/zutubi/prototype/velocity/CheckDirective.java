package com.zutubi.prototype.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.HiddenFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
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
    private ConfigurationRegistry configurationRegistry;
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

            String path = lookupPath();
            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(path, ctype.getSymbolicName());
            formDescriptor.setAction("check");
            
            // decorate the form to include the symbolic name as a hidden field. This is necessary for
            // configuration. This is probably not the best place for this, but until i think of a better location,
            // here it stays.
            HiddenFieldDescriptor hiddenFieldDescriptor = new HiddenFieldDescriptor();
            hiddenFieldDescriptor.setName("symbolicName");
            hiddenFieldDescriptor.setValue(ctype.getSymbolicName());
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
            CompositeType checkType = configurationRegistry.getConfigurationCheckType(ctype);

            FormDescriptor checkFormDescriptor = formDescriptorFactory.createDescriptor(path, checkType);
            for (FieldDescriptor fd : checkFormDescriptor.getFieldDescriptors())
            {
                formDescriptor.add(fd);
            }
            formDescriptor.setActions(Arrays.asList("check"));

            Map<String, Object> context = initialiseContext(checkType.getClazz());

            OgnlValueStack stack = ActionContext.getContext().getValueStack();
            Record data = (Record) stack.findValue("checkRecord");

            Form form = formDescriptor.instantiate(path, data);
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

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
