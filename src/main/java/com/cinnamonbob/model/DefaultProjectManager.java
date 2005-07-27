package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.ProjectDao;

import java.util.List;

/**
 * 
 *
 */
public class DefaultProjectManager implements ProjectManager
{
    private ProjectDao projectDao;

    public void setProjectDao(ProjectDao dao)
    {
        projectDao = dao;
    }

    public void save(Project project)
    {
        projectDao.save(project);
    }

    public Project getProject(String name)
    {
        return projectDao.findByName(name);
    }

    public Project getProject(long id)
    {
        return (Project) projectDao.findById(id);
    }

    public List getProjectsWithNameLike(String name)
    {
        return projectDao.findByLikeName(name);
    }
}
