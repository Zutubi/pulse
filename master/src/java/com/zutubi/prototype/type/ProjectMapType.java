package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

/**
 */
public class ProjectMapType extends MapType
{
    public ProjectMapType(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        super(configurationPersistenceManager);
    }

    public boolean isTemplated()
    {
        return true;
    }
}
