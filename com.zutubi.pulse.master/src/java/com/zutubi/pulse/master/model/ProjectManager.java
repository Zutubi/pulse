package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;

import java.util.Collection;
import java.util.List;

/**
 */
public interface ProjectManager extends EntityManager<Project>
{
    String GLOBAL_PROJECT_NAME     = "global project template";
    String TRIGGER_CATEGORY_MANUAL = "manual";

    @SecureResult
    List<ProjectConfiguration> getAllProjectConfigs(boolean allowInvalid);

    @SecureResult
    ProjectConfiguration getProjectConfig(String name, boolean allowInvalid);

    @SecureResult
    ProjectConfiguration getProjectConfig(long id, boolean allowInvalid);

    @SecureResult
    Project getProject(String name, boolean allowInvalid);

    @SecureResult
    Project getProject(long id, boolean allowInvalid);

    @SecureResult
    List<Project> getProjects(boolean allowInvalid);

    /**
     * Return the accessible list of project instances from the given set of
     * project ids.
     *
     * @param ids the list of ids uniquely identifying a project
     * @param allowInvalid if true, invalid projects will be included in the result
     * @return a list of project instances.
     */
    @SecureResult
    List<Project> getProjects(Collection<Long> ids, boolean allowInvalid);

    boolean isProjectValid(Project project);

    int getProjectCount();

    void abortUnfinishedBuilds(Project project, String message);

    /**
     * Acquires a lock on the state for the given project.  The lock must be
     * held when performing compound logic involving the project state.  The
     * lock is exclusive and reentrant.
     *
     * @param projectId identifier of the project to lock the state for
     *
     * @see #unlockProjectState(long)
     * @see #isProjectStateLocked(long)
     */
    void lockProjectState(long projectId);

    /**
     * Releases a lock on the state for the given project.  The caller must
     * currently hold the lock.
     *
     * @param projectId identifier of the project to unlock the state for
     *
     * @see #lockProjectState(long)
     * @see #isProjectStateLocked(long)
     */
    void unlockProjectState(long projectId);

    /**
     * Indicates if the calling thread holds the state lock for the given
     * project.
     *
     * @param projectId identifier of the project to test the state lock of
     * @return true iff the given project's state lock is held by the calling
     *         thread
     */
    boolean isProjectStateLocked(long projectId);

    /**
     * Performs a state transition on a project if possible.  This is the only
     * way that a project state should be updated, to avoid race conditions.
     * This operation is atomic via use of the state lock: the implementation
     * itself does the necessary locking so the caller is not required to do
     * so.  In cases where the caller needs to inspect the state first or
     * perform other operations atomically with the transition, however, the
     * caller should increase the scope of the locking as required using
     * {@link #lockProjectState(long)}.  State locks are reentrant to allow
     * this pattern.
     * <p/>
     * If the project transitions into a state which requires further action,
     * the project manager will initiate that action after updating the state.
     * For example, making the {@link com.zutubi.pulse.master.model.Project.Transition#DELETE}
     * transition will lead to the project state being deleted.
     *
     * @param projectId  identifier of the project to update
     * @param transition the transition to make
     * @return true iff the project was found and the transition successfully
     *         made
     */
    boolean makeStateTransition(long projectId, Project.Transition transition);

    void delete(Project project);

    void save(Project project);

    /**
     * Triggers a build of the given project by raising appropriate build
     * request events.  Multiple events may be raised depending on
     * configuration, for example if the project is marked for changelist
     * isolation.
     *
     * @param project       the project to trigger a build of
     * @param options       the options for the build being triggered.
     * @param revision      the revision to build, or null if the revision is not fixed
     * (in which case changelist isolation may result in multiple build requests).
     *
     * @see TriggerOptions
     */
    @SecureParameter(action = ProjectConfigurationActions.ACTION_TRIGGER, parameterType = ProjectConfiguration.class)
    void triggerBuild(ProjectConfiguration project, TriggerOptions options, Revision revision);

    // Personal builds are shielded by their own permission, not the trigger
    // authority.
    void triggerBuild(long number, Project project, User user, Revision revision, PatchArchive archive) throws PulseException;

    @SecureParameter(action = AccessManager.ACTION_VIEW)
    long getNextBuildNumber(Project project);

    // These are secured as they use mapConfigsToProjects underneath
    Collection<ProjectGroup> getAllProjectGroups();
    ProjectGroup getProjectGroup(String name);

    @SecureResult
    List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects);

    void removeReferencesToAgent(long agentStateId);

    @SecureParameter(action = ProjectConfigurationActions.ACTION_TRIGGER)
    void markForCleanBuild(Project project);

    /**
     * Sets the last poll time for the project to the given timestamp.
     *
     * @param projectId id of the project to update
     * @param timestamp the new last poll time in milliseconds since the epoch
     */
    void updateLastPollTime(long projectId, long timestamp);
}
