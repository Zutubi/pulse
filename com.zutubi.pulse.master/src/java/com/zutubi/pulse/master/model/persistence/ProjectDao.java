package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;

import java.util.List;

/**
 * Provides access to project entities.
 */
public interface ProjectDao extends EntityDao<Project>
{
    public List<Project> findByResponsible(User user);
}
