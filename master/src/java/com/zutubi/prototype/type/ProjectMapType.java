package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

/**
 */
public class ProjectMapType extends MapType
{
    private ProjectManager projectManager;

    public ProjectMapType(Class type, ProjectManager projectManager)
    {
        super(type);
        this.projectManager = projectManager;
    }

    protected String getNextKey(String path, Record record, RecordManager recordManager)
    {
        Project project = new Project((String) record.get("name"), (String) record.get("description"));
        projectManager.save(project);
        return Long.toString(project.getId());
    }
}
