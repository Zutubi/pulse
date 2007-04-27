package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
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

    public Map<String, ProjectConfiguration> getAllProjectConfigs()
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

    public Project getProjectByBuildSpecification(BuildSpecification buildSpecification)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Project> getNameToConfig()
    {
        return new LinkedList<Project>(projects.values());
    }

    public List<Project> getAllProjectsCached()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<Project> getProjectsWithNameLike(String s)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getProjectCount()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void save(BuildSpecification specification)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void setDefaultBuildSpecification(Project project, long specId)
    {
        throw new RuntimeException("Method not yet implemented.");
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

    public void create(Project project)
    {
        save(project);
    }

    public void checkWrite(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public Project cloneProject(Project project, String name, String description)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public long getNextBuildNumber(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void save(CommitMessageTransformer transformer)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public CommitMessageTransformer getCommitMessageTransformer(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(CommitMessageTransformer transformer)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<CommitMessageTransformer> getCommitMessageTransformers()
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<CommitMessageTransformer> findCommitMessageTransformersByProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public CommitMessageTransformer findCommitMessageTransformerByName(String name)
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

    public BuildSpecification getBuildSpecification(long id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(BuildHostRequirements hostRequirements)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void updateProjectDetails(Project project, String name, String description, String url)
    {
        throw new RuntimeException("Method not implemented.");
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

    public void triggerBuild(Project project, String specification, BuildReason reason, Revision revision, boolean force)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void triggerBuild(long number, Project project, BuildSpecification specification, User user, PatchArchive archive)
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
