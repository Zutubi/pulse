package com.zutubi.pulse.model;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import org.acegisecurity.annotation.Secured;

import java.util.Collection;
import java.util.List;

/**
 * 
 *
 */
public interface ProjectManager extends EntityManager<Project>
{
    Collection<ProjectConfiguration> getAllProjectConfigs();

    ProjectConfiguration getProjectConfig(String name);

    ProjectConfiguration getProjectConfig(long id);

    void saveProjectConfig(ProjectConfiguration config);

    Project getProject(String name);

    Project getProject(long id);

    List<Project> getProjects();

    List<Project> getAllProjectsCached();

    int getProjectCount();

    void buildCommenced(long projectId);

    void buildCompleted(long projectId);

    @Secured({"ACL_PROJECT_WRITE"})
    Project pauseProject(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void resumeProject(Project project);

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(Project project);

    @Secured({"ROLE_ADMINISTRATOR", "ACL_PROJECT_WRITE"})
    void save(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void checkWrite(Project project);

    /**
     * Returns a list of all projects that allow administration by the given
     * authority.
     *
     * @param authority authority to search by
     * @return the projects that may be administered by principles with the
     *         given authority
     */
    @Secured({"ROLE_ADMINISTRATOR"})
    List<Project> getProjectsWithAdmin(String authority);

    /**
     * Updates the projects that allow administration by principles with the
     * given authority.
     *
     * @param authority authority to update
     * @param restrictToProjects if null, allow the authority to administer
     *                           all projects, otherwise, restrict the
     *                           authority to only administer the given
     *                           projects
     */
    @Secured({"ROLE_ADMINISTRATOR"})
    void updateProjectAdmins(String authority, List<Long> restrictToProjects);

    /**
     * Deletes all project ACL entries that are granted to the given
     * authority.
     *
     * @param authority the authority to remove all ACLs for
     */
    @Secured({"ROLE_ADMINISTRATOR"})
    void removeAcls(String authority);

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
    void triggerBuild(ProjectConfiguration project, BuildReason reason, Revision revision, boolean force);

    void triggerBuild(long number, Project project, User user, PatchArchive archive) throws PulseException;

    long getNextBuildNumber(Project project);

    void delete(BuildHostRequirements hostRequirements);

    List<ProjectGroup> getAllProjectGroups();
    List<ProjectGroup> getAllProjectGroupsCached();
    ProjectGroup getProjectGroup(long id);
    ProjectGroup getProjectGroup(String name);

    @Secured({"ROLE_ADMINISTRATOR"})
    void save(ProjectGroup projectGroup);

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(ProjectGroup projectGroup);
}
