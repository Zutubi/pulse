package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.PersistenceManager;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class SaveAction extends ActionSupport
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
        if (!TextUtils.stringSet(symbolicName))
        {
            return INPUT;
        }

        // pass in a validation context for error reporting.
        persistenceManager.saveToStore(path, symbolicName, ActionContext.getContext().getParameters());

        configuration = new Configuration(path);
        configuration.setConfigurationRegistry(configurationRegistry);
        configuration.setRecordManager(recordManager);
        configuration.setTypeRegistry(typeRegistry);
        configuration.analyse();

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
