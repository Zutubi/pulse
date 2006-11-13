package com.zutubi.pulse.model;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.scheduling.SchedulingException;
import org.acegisecurity.annotation.Secured;

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

    Project getProjectByScm(long scmId);

    Project getProjectByBuildSpecification(BuildSpecification buildSpecification);

    List<Project> getAllProjects();

    List<Project> getProjectsWithNameLike(String s);

    int getProjectCount();

    void save(BuildSpecification specification);

    @Secured({"ACL_PROJECT_WRITE"})
    void setDefaultBuildSpecification(Project project, long specId);

    /**
     * Deletes a build specification *and* all triggers that refer to it.
     *
     * @param project   the project to delete the specification from
     * @param specId    the identifier of the specification to delete
     * @throws com.zutubi.pulse.core.PulseRuntimeException
     *          if there is no
     *          specification with the given identifier, or an error occurs
     *          while deleting it
     */
    @Secured({"ACL_PROJECT_WRITE"})
    void deleteBuildSpecification(Project project, long specId);

    @Secured({"ACL_PROJECT_WRITE"})
    void deleteArtifact(Project project, long id);

    void buildCommenced(long projectId);

    void buildCompleted(long projectId);

    @Secured({"ACL_PROJECT_WRITE"})
    Project pauseProject(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void resumeProject(Project project);

    @Secured({"ROLE_ADMINISTRATOR"})
    void create(Project project) throws LicenseException;

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(Project project);

    @Secured({"ROLE_ADMINISTRATOR", "ACL_PROJECT_WRITE"})
    void save(Project project);

    @Secured({"ACL_PROJECT_WRITE"})
    void checkWrite(Project project);

    /**
     * Creates and saves a project that is a replica of the given project,
     * but with the given name.
     *
     * @param project the project to copy
     * @param name    the name of the new project
     * @return the new project
     */
    @Secured({"ROLE_ADMINISTRATOR"})
    Project cloneProject(Project project, String name, String description);

    /**
     * Updates the basic details of a project to the given values, adjusting
     * other persistent entities where necessary (e.g. trigger groups).
     *
     * @param project     the project to be updated
     * @param name        the new name for the project
     * @param description the new description for the project
     * @param url         the new url for the project
     */
    @Secured({"ACL_PROJECT_WRITE"})
    void updateProjectDetails(Project project, String name, String description, String url) throws SchedulingException;

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
     * Triggers a build of the given specification of the given project by
     * raising appropriate build request events.  Multiple events may be
     * raised depending on configuration, for example if the build
     * specification is marked for changelist isolation.
     *
     * @param project       the project to trigger a build of
     * @param specification name of the specification to build
     * @param reason        the reason the build was triggered
     * @param revision      the revision to build, or null if the revision is
     *                      not fixed (in which case changelist isolation may
     *                      result in multiple build requests
     * @param force         if true, force a build to occur even if the
     *                      latest has been built
     */
    void triggerBuild(Project project, String specification, BuildReason reason, Revision revision, boolean force);

    void triggerBuild(long number, Project project, BuildSpecification specification, User user, PatchArchive archive) throws PulseException;

    long getNextBuildNumber(Project project);

    List<ProjectGroup> getAllProjectGroups();
    ProjectGroup getProjectGroup(long id);
    ProjectGroup getProjectGroup(String name);

    @Secured({"ROLE_ADMINISTRATOR"})
    void save(ProjectGroup projectGroup);

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(ProjectGroup projectGroup);
}
