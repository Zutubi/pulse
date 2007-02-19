package com.zutubi.prototype.webwork;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.PersistenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

/**
 *
 *
 */
public class ResetAction extends ActionSupport
{
    private PersistenceManager persistenceManager;
    private RecordManager recordManager;
    private ConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;

    private String symbolicName;
    private String path;
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
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
        if (!TextUtils.stringSet(path))
        {
            return INPUT;
        }

        configuration = new Configuration(path);
        configuration.setConfigurationRegistry(configurationRegistry);
        configuration.setRecordManager(recordManager);
        configuration.setTypeRegistry(typeRegistry);
        configuration.analyse();

        persistenceManager.delete(path);

        return SUCCESS;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager)
    {
        this.persistenceManager = persistenceManager;
    }
}
