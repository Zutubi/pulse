package com.zutubi.pulse.master.tove.velocity;

import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.model.*;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.Configuration;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;
import org.mortbay.http.EOFException;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class FormDirective extends ToveDirective
{
    private static final Logger LOG = Logger.getLogger(FormDirective.class);

    private FormDescriptorFactory formDescriptorFactory;
    private String action;
    private String formName = "mainForm";
    private boolean displayMode = false;
    private boolean ajax = false;
    private String namespace;

    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationTemplateManager configurationTemplateManager;
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
        this.namespace = namespace;
    }

    public boolean render(InternalContextAdapter contextAdapter, Writer writer, Node node)
    {
        try
        {
            Map params = createPropertyMap(contextAdapter, node);
            wireParams(params);

            CompositeType ctype = (CompositeType) lookupType();
            Record data = lookupRecord();

            String path = lookupPath();
            FormDescriptor formDescriptor = formDescriptorFactory.createDescriptor(PathUtils.getParentPath(path), PathUtils.getBaseName(path), ctype, configurationTemplateManager.isConcrete(path), formName);
            formDescriptor.setDisplayMode(displayMode);
            formDescriptor.setReadOnly(!configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE));
            formDescriptor.setAjax(ajax);
            formDescriptor.setNamespace(namespace);
            if (ctype.getClazz().equals(LabelConfiguration.class))
            {
                formDescriptor.setAction("saveLabel");
            }
            // These decorations should be genericised
            if (displayMode)
            {
                formDescriptor.setActions("apply", "reset");
            }

            if (data != null && data instanceof TemplateRecord)
            {
                TemplateFormDecorator templateDecorator = new TemplateFormDecorator((TemplateRecord) data);
                templateDecorator.decorate(formDescriptor);
            }

            // Decorate the form to include the symbolic name as a hidden field. This is necessary for configuration.
            // This is probably not the best place for this, but until i think of a better location, it stays.
            HiddenFieldDescriptor hiddenFieldDescriptor = new HiddenFieldDescriptor();
            hiddenFieldDescriptor.setName("symbolicName");
            hiddenFieldDescriptor.setValue(ctype.getSymbolicName());
            formDescriptor.add(hiddenFieldDescriptor);

            // Create the context object used to define the freemarker rendering context
            Class clazz = ctype.getClazz();
            Form form = formDescriptor.instantiate(lookupPath(), data);
            if (StringUtils.stringSet(action))
            {
                form.setAction(action);
            }

            Map<String, Object> context = new HashMap<String, Object>();
            context.put("path", path);
            ToveUtils.renderForm(context, form, clazz, writer, configuration);

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
                writer.write(renderError("Failed to render form. Unexpected " + throwable.getClass() + ": " + throwable.getMessage()));
            }
            catch (IOException e)
            {
                // We did our best.
            }

            return true;
        }
    }

    public void setFormDescriptorFactory(FormDescriptorFactory formDescriptorFactory)
    {
        this.formDescriptorFactory = formDescriptorFactory;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}

