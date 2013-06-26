package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.zutubi.i18n.Messages;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.i18n.context.ClassContext;
import com.zutubi.i18n.context.ContextResolver;
import com.zutubi.i18n.context.ExtendedClassContextResolver;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.velocity.VelocityClasspathResourceLoader;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for tove webwork actions.
 *
 * This class provides a lot of the functionality shared across the various
 * generated pages. 
 *
 */
public class ToveActionSupport extends ActionSupport implements MessagesProvider
{
    private static final Logger LOG = Logger.getLogger(ToveActionSupport.class);

    // These constants are used in numerous locations throughout the code base.
    // Where is the best place to define them?.  Most of the references are in master,
    // with some in the annotations package.
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_APPLY = "apply";
    public static final String ACTION_CONFIRM = "confirm";
    public static final String ACTION_PREVIOUS = "previous";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_INPUT = "input";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_FINISH = "finish";

    protected String path;
    protected ConfigurationResponse response;
    protected ConfigurationUIModel configuration;

    protected TypeRegistry typeRegistry;
    protected MasterConfigurationRegistry configurationRegistry;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationProvider configurationProvider;

    protected Record record;
    
    protected Type type;

    private String submitField;

    private Template template;

    public boolean isCancelled()
    {
        return isCancelSelected();
    }

    public boolean isCancelSelected()
    {
        return isSelected(ACTION_CANCEL) || isSelected(ACTION_RESET);
    }

    public boolean isSaveSelected()
    {
        return isSelected(ACTION_SAVE) || isSelected(ACTION_APPLY);
    }

    public boolean isConfirmSelected()
    {
        return isSelected(ACTION_CONFIRM);
    }

    public boolean isDeleteSelected()
    {
        return isSelected(ACTION_DELETE);
    }

    public boolean isInputSelected()
    {
        return isSelected(ACTION_INPUT);
    }

    public boolean isPreviousSelected()
    {
        return isSelected(ACTION_PREVIOUS);
    }

    public boolean isNextSelected()
    {
        return isSelected(ACTION_NEXT);
    }

    public boolean isFinishSelected()
    {
        return isSelected(ACTION_FINISH);
    }

    public boolean isSelected(String s)
    {
        return s.equals(submitField);
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public ConfigurationResponse getConfigurationResponse()
    {
        return response;
    }

    public ConfigurationErrors getConfigurationErrors()
    {
        return new ConfigurationErrors(this);
    }

    public Type getType()
    {
        return type;
    }

    public ConfigurationUIModel getConfiguration()
    {
        return configuration;
    }

    public Record getRecord()
    {
        return record;
    }

    protected void prepare()
    {
        // default handling - render the page.
        configuration = objectFactory.buildBean(ConfigurationUIModel.class, path);
        configuration.analyse();

        if (record == null)
        {
            record = configuration.getRecord();
        }

        Configuration instance = configuration.getInstance();
        if(instance != null && !instance.isValid())
        {
            ToveUtils.mapErrors(instance, this, null);
        }

        type = configuration.getType();
    }

    public String doRender() throws Exception
    {
        prepare();

        // a) do we have a custom template for rendering this type / instance?.
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            template = lookupTemplate(compositeType.getClazz());
            if (template != null)
            {
                return "custom";
            }

            // default.
            return "composite";
        }
        if (type instanceof CollectionType)
        {
            // default for collections.
            return "map";
        }

        // unknown type.
        return ERROR;
    }

    protected boolean isParentEmbeddedCollection()
    {
        String parentPath = PathUtils.getParentPath(path);
        if (parentPath == null)
        {
            return false;
        }

        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        return ToveUtils.isEmbeddedCollection(parentType);
    }

    /**
     * Find and return (if available) a custom velocity template to use when rendering
     * the specified configuration type.
     *
     * @param clazz the type for which we are looking up the custom template.
     *
     * @return a velocity template instance if located, null otherwise.
     */
    private Template lookupTemplate(Class<? extends Configuration> clazz)
    {
        final List<String> paths = resolve(new ClassContext(clazz));

        final VelocityEngine engine = VelocityManager.getInstance().getVelocityEngine();

        if (engine == null)
        {
            // This happens if the velocity system within webwork has not finished initialisation. The
            // best option available to us is to assume no custom template is available.
            return null;
        }

        return executeVelocityOperation(clazz, new NullaryFunction<Template>()
        {
            public Template process()
            {
                for (String path : paths)
                {
                    String templatePath = path + ".template.vm";
                    if (engine.templateExists(templatePath))
                    {
                        try
                        {
                            return engine.getTemplate(templatePath);
                        }
                        catch (Exception e)
                        {
                            // there is a problem with the template we tried to load.  Swallow the
                            // error and keep going.
                            LOG.warning(e);
                        }
                    }
                }
                return null;
            }
        });
    }

    private List<String> resolve(ClassContext context)
    {
        List<String> resolvedNames = new LinkedList<String>();
        ContextResolver<ClassContext> resolver = new ExtendedClassContextResolver();
        resolvedNames.addAll(Arrays.asList(resolver.resolve(context)));
        return resolvedNames;
    }

    private <T> T executeVelocityOperation(Object context, NullaryFunction<T> f)
    {
        // HAX: We need the context to be part of the resource search, but can not dynamically configure
        // a resource loader within the already configured velocity engine.  So, we use a thread local
        // to pass the context through to the pre-configured resource loader, and make sure we cleanup after
        //  ourselves.
        VelocityClasspathResourceLoader.CONTEXT.set((Class)context);
        try
        {
            return f.process();
        }
        finally
        {
            VelocityClasspathResourceLoader.CONTEXT.set(null);
        }
    }

    public Template getTemplate()
    {
        return template;
    }

    public Messages getMessages()
    {
        return Messages.getInstance(type.getTargetType().getClazz());
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(MasterConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
