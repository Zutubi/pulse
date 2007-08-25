package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RunExecutablePostBuildAction;
import com.zutubi.pulse.model.TagPostBuildAction;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectDao extends EntityDao<Project>
{
    void save(TagPostBuildAction action);

    void save(RunExecutablePostBuildAction action);

    TagPostBuildAction findTagPostBuildAction(long id);

    RunExecutablePostBuildAction findRunExecutablePostBuildAction(long id);

    List<Project> findByAdminAuthority(String authority);

    void delete(BuildHostRequirements hostRequirements);
}
