package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.ProjectGroup;

/**
 */
public interface ProjectGroupDao extends EntityDao<ProjectGroup>
{
    ProjectGroup findByName(String name);
}
