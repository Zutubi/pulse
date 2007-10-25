package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.AgentRequirements;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.security.SecureParameter;
import com.zutubi.prototype.security.AccessManager;

import java.util.*;

/**
 */
public class MockProjectManager implements ProjectManager
{
    private Map<Long, Project> projects = new TreeMap<Long, Project>();
    private long nextId = 1;

    public List<ProjectConfiguration> getAllProjectConfigs(boolean allowInvalid)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public ProjectConfiguration getProjectConfig(String name, boolean allowInvaid)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public ProjectConfiguration getProjectConfig(long id, boolean allowInvalid)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Project getProject(String name, boolean allowInvalid)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public Project getProject(long id, boolean allowInvalid)
    {
        return projects.get(id);
    }

    public List<Project> getProjects(boolean allowInvalid)
    {
        return new LinkedList<Project>(projects.values());
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

    public void checkWrite(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public long getNextBuildNumber(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Collection<ProjectGroup> getAllProjectGroups()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public ProjectGroup getProjectGroup(String name)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void removeReferencesToAgent(long agentStateId)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void markForCleanBuild(Project project)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void delete(AgentRequirements hostRequirements)
    {
        throw new RuntimeException("Method not yet implemented.");
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
