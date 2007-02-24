package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectGroup;

import java.util.List;

/**
 */
public interface ProjectGroupDao extends EntityDao<ProjectGroup>
{
    ProjectGroup findByName(String name);
    
    List<ProjectGroup> findByProject(Project project);

    List<ProjectGroup> findAllCached();
}
