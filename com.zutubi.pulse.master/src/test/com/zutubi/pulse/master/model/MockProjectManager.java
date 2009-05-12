package com.zutubi.pulse.master.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MockProjectManager extends DefaultProjectManager
{
    private Map<Long, Project> projects = new TreeMap<Long, Project>();
    private long nextId = 1;

    public Project getProject(long id, boolean allowInvalid)
    {
        return projects.get(id);
    }

    public List<Project> getProjects(boolean allowInvalid)
    {
        return new LinkedList<Project>(projects.values());
    }

    public void save(Project project)
    {
        if (project.getId() == 0)
        {
            project.setId(nextId++);
        }
        projects.put(project.getId(), project);
    }

    public void removeReferencesToUser(User user)
    {
        throw new RuntimeException("Not implemented");
    }

    public void updateLastPollTime(long projectId, long timestamp)
    {
        throw new RuntimeException("Method not yet implemented");
    }

    public void delete(Project project)
    {
        projects.remove(project.getId());
    }
}
