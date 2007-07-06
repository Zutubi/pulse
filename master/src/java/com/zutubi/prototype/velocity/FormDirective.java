package com.zutubi.prototype.velocity;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.FormDescriptor;
import com.zutubi.prototype.FormDescriptorFactory;
import com.zutubi.prototype.TemplateFormDecorator;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.model.HiddenFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import com.zutubi.util.logging.Logger;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
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
    private String action;
    private String formName = "mainForm";
    private boolean displayMode = false;
    private boolean ajax = false;
    private String namespace;
    
    private MasterConfigurationManager configurationManager;

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

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    public void setDisplayMode(boolean displayMode)
    {
        this.displayMode = displayMode;
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }

    /**
     * The namespace defines the url namespace that this form is being rendered in.  This is used by
     * the form generation process to determine the correct url to submit the form to.
     *
     * @param namespace in which this form is operating.
     */
    public void setNamespace(String namespace)
    {
        //TODO: Is there a way to extract this automatically?  It is used via PathUtils.getConfigPath
        this.namespace = namespace;
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

            String path = lookupPath();
            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(PathUtils.getParentPath(path), PathUtils.getBaseName(path), ctype, formName);
            formDescriptor.setDisplayMode(displayMode);
            formDescriptor.setAjax(ajax);
            formDescriptor.setNamespace(namespace);

            // These decorations should be genericised
            if(displayMode)
            {
                formDescriptor.setActions("apply", "reset");
            }

            TemplateFormDecorator templateDecorator = new TemplateFormDecorator(data);
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
            if(TextUtils.stringSet(action))
            {
                form.setAction(action);
            }

            context.put("form", form);

            // Get our own configuration so that we can mess with the
            // tenplate loader
            Configuration configuration = FreemarkerConfigurationFactoryBean.createConfiguration(configurationManager);
            TemplateLoader currentLoader = configuration.getTemplateLoader();
            TemplateLoader classLoader = new ClassTemplateLoader(ctype.getClazz(), "");
            MultiTemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{ classLoader, currentLoader });
            configuration.setTemplateLoader(loader);
            
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}

