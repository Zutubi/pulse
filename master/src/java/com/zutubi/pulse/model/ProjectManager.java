package com.zutubi.pulse.model;

import com.zutubi.prototype.security.AccessManager;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.security.SecureParameter;
import com.zutubi.pulse.security.SecureResult;

import java.util.Collection;
import java.util.List;

/**
 */
public interface ProjectManager extends EntityManager<Project>
{
    String GLOBAL_PROJECT_NAME = "global project template";

    @SecureResult
    List<ProjectConfiguration> getAllProjectConfigs(boolean allowInvalid);

    @SecureResult
    ProjectConfiguration getProjectConfig(String name, boolean allowInvalid);

    @SecureResult
    ProjectConfiguration getProjectConfig(long handle, boolean allowInvalid);

    @SecureResult
    Project getProject(String name, boolean allowInvalid);

    @SecureResult
    Project getProject(long id, boolean allowInvalid);

    @SecureResult
    List<Project> getProjects(boolean allowInvalid);

    int getProjectCount();

    void buildCommenced(long projectId);

    void buildCompleted(long projectId, boolean successful);

    @SecureParameter(action = ProjectConfigurationActions.ACTION_PAUSE)
    Project pauseProject(Project project);

    @SecureParameter(action = ProjectConfigurationActions.ACTION_PAUSE)
    void resumeProject(Project project);

    @SecureParameter(action = AccessManager.ACTION_DELETE)
    void delete(Project project);

    void save(Project project);

    @SecureParameter(action = AccessManager.ACTION_WRITE)
    void checkWrite(Project project);

    /**
     * Triggers a build of the given project by raising appropriate build
     * request events.  Multiple events may be raised depending on
     * configuration, for example if the project is marked for changelist
     * isolation.
     *
     * @param project       the project to trigger a build of
     * @param reason        the reason the build was triggered
     * @param revision      the revision to build, or null if the revision is
     *                      not fixed (in which case changelist isolation may
     *                      result in multiple build requests
     * @param force         if true, force a build to occur even if the
     *                      latest has been built
     */
    @SecureParameter(action = ProjectConfigurationActions.ACTION_PAUSE, parameterType = ProjectConfiguration.class)
    void triggerBuild(ProjectConfiguration project, BuildReason reason, Revision revision, boolean force);

    @SecureParameter(action = ProjectConfigurationActions.ACTION_PAUSE, parameterType = Project.class)
    void triggerBuild(long number, Project project, User user, PatchArchive archive) throws PulseException;

    @SecureParameter(action = AccessManager.ACTION_VIEW)
    long getNextBuildNumber(Project project);

    void delete(BuildHostRequirements hostRequirements);

    // These are secured as they use mapConfigsToProjects underneath
    Collection<ProjectGroup> getAllProjectGroups();
    ProjectGroup getProjectGroup(String name);

    @SecureResult
    List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects);
}
