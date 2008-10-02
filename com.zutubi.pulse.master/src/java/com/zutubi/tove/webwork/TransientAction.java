package com.zutubi.tove.webwork;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.master.web.ActionSupport;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;

/**
 * Base for actions that use transient configuration.
 */
public abstract class TransientAction<T> extends ActionSupport implements MessagesProvider
{
    private static final Logger LOG = Logger.getLogger(TransientAction.class);
    
    protected String path;
    protected boolean ajax;
    protected ConfigurationUIModel configuration;

    protected TypeRegistry typeRegistry;
    protected ConfigurationTemplateManager configurationTemplateManager;

    protected Record record;
    private CompositeType type;
    private String symbolicName;
    private String submitField;

    protected TransientAction(String path)
    {
        this.path = path;
    }

    protected TransientAction(String path, boolean ajax)
    {
        this(path);
        this.ajax = ajax;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }
    
    public boolean isCancelSelected()
    {
        if (TextUtils.stringSet(submitField))
        {
            return submitField.equals("cancel");
        }
        else
        {
            return TextUtils.stringSet(cancel);
        }
    }

    public void setSubmitField(String submitField)
    {
        this.submitField = submitField;
    }

    public String getPath()
    {
        return path;
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

    public Messages getMessages()
    {
        return Messages.getInstance(type.getTargetType().getClazz());
    }

    public void analyse()
    {
        configuration = new ConfigurationUIModel(path);
        type = (CompositeType) configuration.getType();
    }

    public String doInput() throws Exception
    {
        // By default, just show an empty form
        analyse();
        T instance = initialise();
        if (instance != null)
        {
            // Does this ever happen, and if so, what then... no initial values i suppose?
            record = type.unstantiate(instance);
        }
        return ajax ? "render" : INPUT;
    }

    @SuppressWarnings({"unchecked"})
    public String execute()
    {
        if(isCancelSelected())
        {
            return "cancel";
        }
        
        if (!TextUtils.stringSet(symbolicName))
        {
            analyse();
            return INPUT;
        }

        type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            analyse();
            return INPUT;
        }

        record = ToveUtils.toRecord(type, ActionContext.getContext().getParameters());

        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);
        Configuration instance = null;
        try
        {
            instance = configurationTemplateManager.validate(parentPath, baseName, record, true, false);
            if (!instance.isValid())
            {
                ToveUtils.mapErrors(instance, this, null);
            }
        }
        catch (TypeException e)
        {
            addActionError(e.getMessage());
        }

        if(hasErrors())
        {
            return INPUT;
        }

        try
        {
            return complete((T) instance);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            addActionError(e.getMessage());
            return ERROR;
        }
    }

    protected abstract T initialise() throws Exception;
    protected abstract String complete(T instance) throws Exception;

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
