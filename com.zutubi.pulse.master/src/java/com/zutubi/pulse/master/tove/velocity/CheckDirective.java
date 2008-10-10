package com.zutubi.pulse.master.tove.velocity;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.tove.FormDescriptor;
import com.zutubi.tove.FormDescriptorFactory;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.model.Form;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.webwork.ToveUtils;
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
public class CheckDirective extends ToveDirective
{
    private static final Logger LOG = Logger.getLogger(CheckDirective.class);

    private String action = "check";
    private String mainFormName = "form";
    private String checkFormName = "check";
    private String symbolicName;

    private FormDescriptorFactory formDescriptorFactory;
    private ConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;
    private Configuration configuration;

    private String namespace;

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

    public void setMainFormName(String mainFormName)
    {
        this.mainFormName = mainFormName;
    }

    public void setCheckFormName(String checkFormName)
    {
        this.checkFormName = checkFormName;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(contextAdapter, node);
            wireParams(params);

            String path = lookupPath();
            CompositeType type = typeRegistry.getType(symbolicName);
            CompositeType checkType = configurationRegistry.getConfigurationCheckType(type);

            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(PathUtils.getParentPath(path), null, checkType, true, "check");
            formDescriptor.setName(checkFormName);
            formDescriptor.setAction(action);
            formDescriptor.setActions("check");
            formDescriptor.setNamespace(namespace);
            formDescriptor.setAjax(true);

            Map<String, Object> context = ToveUtils.initialiseContext(checkType.getClazz());
            OgnlValueStack stack = ActionContext.getContext().getValueStack();
            Record data = (Record) stack.findValue("checkRecord");

            Form form = formDescriptor.instantiate(path, data);
            context.put("form", form);
            context.put("mainFormName", mainFormName);

            try
            {
                Template template = configuration.getTemplate("tove/xhtml/test-form.ftl");
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

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
