package com.cinnamonbob.model;

/**
 * 
 *
 */
public interface ProjectManager 
{
    void save(Project project);

    Project getProject(String name);
    Project getProject(long id);
}
