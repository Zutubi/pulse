package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.i18n.Messages;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.web.ActionSupport;

/**
 * Base for actions that use transient configuration.
 */
public abstract class TransientAction<T> extends ActionSupport implements MessagesProvider
{
    protected String path;
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
            record = type.unstantiate(instance);
        }
        return INPUT;
    }

    @SuppressWarnings({"unchecked"})
    public String execute() throws Exception
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

        record = PrototypeUtils.toRecord(type, ActionContext.getContext().getParameters());

        String parentPath = PathUtils.getParentPath(path);
        String baseName = PathUtils.getBaseName(path);
        Configuration instance = null;
        try
        {
            instance = configurationTemplateManager.validate(parentPath, baseName, record);
            if (!instance.isValid())
            {
                PrototypeUtils.mapErrors(instance, this, null);
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

        return complete((T) instance);
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
