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
    private ProjectManager projectManager;

    public ProjectMapType(ConfigurationPersistenceManager configurationPersistenceManager, ProjectManager projectManager)
    {
        super(configurationPersistenceManager);
        this.projectManager = projectManager;
    }

    public boolean isTemplated()
    {
        return true;
    }

    protected String getNextKey(String path, Record record, RecordManager recordManager)
    {
        Project project = new Project();
        projectManager.save(project);
        return Long.toString(project.getId());
    }
}
