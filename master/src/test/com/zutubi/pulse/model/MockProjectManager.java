package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.*;

/**
 */
public class MockProjectManager implements ProjectManager
{
    private Map<Long, Project> projects = new TreeMap<Long, Project>();
    private long nextId = 1;

    public Collection<ProjectConfiguration> getAllProjectConfigs()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public ProjectConfiguration getProjectConfig(String name)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public ProjectConfiguration getProjectConfig(long id)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void saveProjectConfig(ProjectConfiguration config)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Project getProject(String name)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Project getProject(long id)
    {
        return projects.get(id);
    }

    public List<Project> getProjects()
    {
        return new LinkedList<Project>(projects.values());
    }

    public List<Project> getAllProjectsCached()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getProjectCount()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void buildCommenced(long projectId)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void buildCompleted(long projectId, boolean b)
    {
        // do nothing
    }

    public Project pauseProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void resumeProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void create(Project project)
    {
        save(project);
    }

    public void checkWrite(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public long getNextBuildNumber(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<ProjectGroup> getAllProjectGroups()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<ProjectGroup> getAllProjectGroupsCached()
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public ProjectGroup getProjectGroup(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public ProjectGroup getProjectGroup(String name)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void save(ProjectGroup projectGroup)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(ProjectGroup projectGroup)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Project> mapConfigsToProjects(List<ProjectConfiguration> projects)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void delete(BuildHostRequirements hostRequirements)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public List<Project> getProjectsWithAdmin(String authority)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void updateProjectAdmins(String authority, List<Long> restrictToProjects)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void removeAcls(String authority)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void triggerBuild(ProjectConfiguration project, BuildReason reason, Revision revision, boolean force)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void triggerBuild(long number, Project project, User user, PatchArchive archive)
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
