package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class ConfigurationAction extends ActionSupport
{
    private ConfigurationRegistry configurationRegistry;

    private RecordManager recordManager;

    private TypeRegistry typeRegistry;

    /**
     * The path identifying the configuration presented by this action.
     */
    private String path;

    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }


    public String execute() throws Exception
    {
        configuration = new Configuration(path);
        configuration.setConfigurationRegistry(configurationRegistry);
        configuration.setRecordManager(recordManager);
        configuration.setTypeRegistry(typeRegistry);
        configuration.analyse();

        Type type = configuration.getInstanceType();
        if (type == null)
        {
            type = configuration.getType();
        }

        if (type instanceof CompositeType)
        {
            return "composite";
        }
        if (type instanceof ListType)
        {
            return "list";
        }
        if (type instanceof MapType)
        {
            return "map";
        }

        return SUCCESS;
    }

    /**
     * Required resource reference.
     * 
     * @param configurationRegistry instance
     */
    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
