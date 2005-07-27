package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectManager
{
    void save(Project project);

    Project getProject(String name);
    Project getProject(long id);

    List getProjectsWithNameLike(String s);
}
