package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectManager extends EntityManager<Project>
{
    Project getProject(String name);
    Project getProject(long id);

    List<Project> getProjectsWithNameLike(String s);
}
