package com.zutubi.prototype.velocity;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.PersistenceManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.velocity.AbstractDirective;
import freemarker.template.Configuration;

/**
 *
 *
 */
public abstract class PrototypeDirective extends AbstractDirective
{
    protected Configuration configuration;

    protected ConfigurationRegistry configurationRegistry;

    protected RecordManager recordManager;
    protected PersistenceManager persistenceManager;
    protected TypeRegistry typeRegistry;

    protected String path;
    private String symbolicName;

    public PrototypeDirective()
    {
        ComponentContext.autowire(this);
    }

    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void setPath(String path)
    {
        this.path = path;
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

    protected String lookupSymbolicName()
    {
        String symbolicName = this.symbolicName;
        if (symbolicName != null)
        {
            return symbolicName;
        }

        Record record = recordManager.load(path);
        if (record != null)
        {
            symbolicName = record.getSymbolicName();
        }

        if (symbolicName == null)
        {
            Type type = configurationRegistry.getType(path);
            if (CollectionType.class.isAssignableFrom(type.getClass()))
            {
                CollectionType collectionType = (CollectionType) type;
                Type coreType = collectionType.getCollectionType();
                if (coreType instanceof CompositeType)
                {
                    symbolicName = ((CompositeType)coreType).getSymbolicName();
                }
            } else if (type instanceof CompositeType)
            {
                symbolicName = ((CompositeType)type).getSymbolicName();
            }
        }

        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager)
    {
        this.persistenceManager = persistenceManager;
    }
}
