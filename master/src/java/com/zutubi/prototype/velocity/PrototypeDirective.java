package com.zutubi.prototype.velocity;

import com.zutubi.pulse.velocity.AbstractDirective;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.prototype.ProjectConfigurationManager;
import com.zutubi.prototype.Path;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.config.ConfigurationRegistry;
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

    protected TypeRegistry typeRegistry;

    protected Path path;

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
        this.path = new Path(path);
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
        String symbolicName = null;
        Record record = recordManager.load(path.toString());
        if (record != null)
        {
            symbolicName = record.getMetaProperty("symbolicName");
        }

        if (symbolicName == null)
        {
            symbolicName = configurationRegistry.getSymbolicName(path.toString());
        }
        return symbolicName;
    }
}
