package com.zutubi.pulse.model;

import org.acegisecurity.annotation.Secured;

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

    public Project getProjectByScm(long scmId)
    {
        throw new RuntimeException("Method not implemented.");
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

    public void deleteBuildSpecification(Project project, long specId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    @Secured({"ACL_PROJECT_WRITE"})
    public void deleteArtifact(Project project, long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void buildCommenced(long projectId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void buildCompleted(long projectId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Project pauseProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void resumeProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void checkWrite(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Project cloneProject(Project project, String name, String description)
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
