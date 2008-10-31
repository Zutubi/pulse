package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.i18n.MessagesProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.bean.ObjectFactory;

/**
 *
 *
 */
public class ToveActionSupport extends ActionSupport implements MessagesProvider
{
    public static final String CANCEL = "cancel";
    
    protected String path;
    protected ConfigurationResponse response;
    protected ConfigurationUIModel configuration;

    protected TypeRegistry typeRegistry;
    protected ConfigurationRegistry configurationRegistry;
    protected ConfigurationTemplateManager configurationTemplateManager;
    protected ConfigurationProvider configurationProvider;
    protected ObjectFactory objectFactory;

    protected Record record;
    
    protected Type type;

    private String submitField;


    public boolean isCancelled()
    {
        return isCancelSelected();
    }

    public boolean isCancelSelected()
    {
        return isSelected("cancel") || isSelected("reset");
    }

    public boolean isSaveSelected()
    {
        return "save".equals(submitField) || "apply".equals(submitField);
    }

    public boolean isConfirmSelected()
    {
        return isSelected("confirm");
    }

    public boolean isDeleteSelected()
    {
        return isSelected("delete");
    }

    public boolean isInputSelected()
    {
        return isSelected("input");
    }

    public boolean isPreviousSelected()
    {
        return isSelected("previous");
    }

    public boolean isNextSelected()
    {
        return isSelected("next");
    }

    public boolean isFinishSelected()
    {
        return isSelected("finish");
    }

    private boolean isSelected(String s)
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

    protected void prepare() throws Exception
    {
        // default handling - render the page.
        configuration = objectFactory.buildBean(ConfigurationUIModel.class, new Class[]{String.class}, new Object[]{path});
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

        // TODO: collapse into a single result vm that handles the various types.
        if (type instanceof CompositeType)
        {
            return "composite";
        }
        if (type instanceof CollectionType)
        {
            return "map";
        }

        // unknown type.
        return ERROR;
    }

    public Messages getMessages()
    {
        return Messages.getInstance(type.getTargetType().getClazz());
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
