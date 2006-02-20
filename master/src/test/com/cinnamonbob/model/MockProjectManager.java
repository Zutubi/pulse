package com.cinnamonbob.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class MockProjectManager implements ProjectManager
{
    private Map<Long, Project> projects = new TreeMap<Long, Project>();
    private long nextId = 1;

    public Project getProject(String name)
    {
        for (Project project : projects.values())
        {
            if (project.getName().equals(name))
            {
                return project;
            }
        }

        return null;
    }

    public Project getProject(long id)
    {
        return projects.get(id);
    }

    public List<Project> getAllProjects()
    {
        return new LinkedList<Project>(projects.values());
    }

    public List<Project> getProjectsWithNameLike(String s)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void initialise()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void deleteBuildSpecification(long projectId, long specId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void save(Project project)
    {
        if (project.getId() == 0)
        {
            project.setId(nextId++);
        }
        projects.put(project.getId(), project);
    }

    public void delete(Project project)
    {
        projects.remove(project.getId());
    }
}
