package com.cinnamonbob.model;

import java.util.List;

/**
 * 
 *
 */
public interface ProjectManager extends EntityManager<Project>
{
    /**
     * Looks up the project of the given name.
     * 
     * @param name the name of the project to find
     * @return the relevant project, or null if not found
     */
    Project getProject(String name);
    
    Project getProject(long id);
    
    List<Project> getAllProjects();

    List<Project> getProjectsWithNameLike(String s);

    void initialise();
}
