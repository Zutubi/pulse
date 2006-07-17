package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildRevision;
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

    List<Project> getAllProjects();

    List<Project> getProjectsWithNameLike(String s);

    int getProjectCount();

    void save(BuildSpecification specification);

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

    @Secured({"ROLE_ADMINISTRATOR", "ACL_PROJECT_WRITE"})
    void save(Project project);

    @Secured({"ROLE_ADMINISTRATOR"})
    void delete(Project project);

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
     * Triggers a build of the given specification of the given project by
     * raising appropriate build request events.  Multiple events may be
     * raised depending on configuration, for example if the build
     * specification is marked for changelist isolation.
     *
     * @param project       the project to trigger a build of
     * @param specification name of the specification to build
     * @param reason        the reason the build was triggered
     * @param revision      the revision to build, may be fixed to indicate
     *                      a specific revision, if not fixed then changelist
     *                      isolation may result in multiple build requests
     * @param force         if true, force a build to occur even if the
     *                      latest has been built
     */
    void triggerBuild(Project project, String specification, BuildReason reason, BuildRevision revision, boolean force);

    long getNextBuildNumber(Project project);

    void save(CommitMessageTransformer transformer);
    CommitMessageTransformer getCommitMessageTransformer(long id);
    void delete(CommitMessageTransformer transformer);
    List<CommitMessageTransformer> getCommitMessageTransformers();
    List<CommitMessageTransformer> findCommitMessageTransformersByProject(Project project);
    CommitMessageTransformer findCommitMessageTransformerByName(String name);
}
