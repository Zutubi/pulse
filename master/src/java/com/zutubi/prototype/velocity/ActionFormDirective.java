package com.zutubi.prototype.velocity;

import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.HiddenFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.webwork.PrototypeUtils;
import com.zutubi.pulse.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
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
 * A velocity directive used to render a form that is used to gather
 * arguments for a configuration action.  Most actions have no arguments, but
 * in some cases (e.g. setting a user's password) arguments are required.
 */
public class ActionFormDirective extends PrototypeDirective
{
    private static final Logger LOG = Logger.getLogger(ActionFormDirective.class);

    private FormDescriptorFactory formDescriptorFactory;
    private String formName = "actionForm";
    private String namespace;

    private Configuration configuration = null;

    /**
     * @return the name of this velocity directive.
     */
    public String getName()
    {
        return "actionform";
    }

    public int getType()
    {
        return LINE;
    }

    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * The namespace defines the url namespace that this form is being rendered in.  This is used by
     * the form generation process to determine the correct url to submit the form to.
     *
     * @param namespace in which this form is operating.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(contextAdapter, node);
            wireParams(params);

            CompositeType argumentType = (CompositeType) lookupType();
            String actionName = (String) lookup("actionName");
            String path = lookupPath();

            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(PathUtils.getParentPath(path), PathUtils.getBaseName(path), argumentType, true, formName);
            formDescriptor.setAjax(true);
            formDescriptor.setNamespace(namespace);
            formDescriptor.setAction(actionName);

            HiddenFieldDescriptor hiddenFieldDescriptor = new HiddenFieldDescriptor();
            hiddenFieldDescriptor.setName("newPath");
            hiddenFieldDescriptor.setValue(lookup("newPath"));
            formDescriptor.add(hiddenFieldDescriptor);

            // Create the context object used to define the freemarker rendering context
            Map<String, Object> context = PrototypeUtils.initialiseContext(argumentType.getClazz());

            Form form = formDescriptor.instantiate(lookupPath(), lookupRecord());
            context.put("form", form);

            // Get our own configuration so that we can mess with the
            // template loader

            Configuration configuration = FreemarkerConfigurationFactoryBean.addClassTemplateLoader(argumentType.getClazz(), this.configuration);
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

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
