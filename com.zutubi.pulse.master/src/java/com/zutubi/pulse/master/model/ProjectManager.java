package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * Returns all concrete projects that descend from the project or project
     * template of the given name.
     *
     * @param project      the project to get descendants of, may be the name
     *                     of a project template
     * @param strict       if true, the project itself is not considered for
     *                     inclusion in the result
     * @param allowInvalid if true, invalid projects will be included in the
     *                     result
     * @return all concrete projects that descend from the given project
     */
    @SecureResult
    List<Project> getDescendantProjects(String project, boolean strict, boolean allowInvalid);

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

    /**
     * A check to see whether or not the specified project configuration is
     * valid.
     *
     * @param project   the configuration in question.
     *
     * @return true if the configuration is valid, false otherwise.
     */
    boolean isProjectValid(ProjectConfiguration project);

    int getProjectCount();

    void abortUnfinishedBuilds(Project project, String message);

    /**
     * Runs the given callback function while holding state locks for all of
     * the given projects.  Whenever basing decisions on project states, these
     * locks must be held.  The acquisition and release of these locks is
     * controlled by the project manager so that it can prevent possible
     * deadlocks.
     * 
     * @param fn         callback to run while holding the locks
     * @param projectIds ids of the projects to lock
     */
    void runUnderProjectLocks(Runnable fn, long... projectIds);

    /**
     * Performs a state transition on a project if possible.  This is the only
     * way that a project state should be updated, to avoid race conditions.
     * This operation is atomic via use of the state lock: the implementation
     * itself does the necessary locking so the caller is not required to do
     * so.  In cases where the caller needs to inspect the state first or
     * perform other operations atomically with the transition, however, the
     * caller should increase the scope of the locking as required using
     * {@link #runUnderProjectLocks(Runnable, long...)}.  State locks are
     * reentrant to allow this pattern.
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
     * @return the ids of the build requests, used to track the requests via
     *         the {@link com.zutubi.pulse.master.build.queue.BuildRequestRegistry}, may
     *         be empty if no request was made
     *
     * @see TriggerOptions
     */
    @SecureParameter(action = ProjectConfigurationActions.ACTION_TRIGGER, parameterType = ProjectConfiguration.class)
    List<Long> triggerBuild(ProjectConfiguration project, TriggerOptions options, Revision revision);

    // Personal builds are shielded by their own permission, not the trigger
    // authority.
    long triggerBuild(long number, Project project, User user, Revision revision, File patchFile, String patchFormat) throws PulseException;

    @SecureParameter(action = AccessManager.ACTION_VIEW)
    long getNextBuildNumber(Project project, boolean allocate);

    // These are secured as they use mapConfigsToProjects underneath
    Collection<ProjectGroup> getAllProjectGroups();
    ProjectGroup getProjectGroup(String name);

    @SecureResult
    List<Project> mapConfigsToProjects(Collection<ProjectConfiguration> projects);

    /**
     * Removes all references from projects to a given user (used when the user
     * is deleted).
     *
     * @param user the user to remove all references to
     */
    void removeReferencesToUser(User user);

    /**
     * Sets the last poll time for the project to the given timestamp.
     *
     * @param projectId id of the project to update
     * @param timestamp the new last poll time in milliseconds since the epoch
     */
    void updateLastPollTime(long projectId, long timestamp);

    /**
     * Finds all projects that the given user is responsible for.
     *
     * @param user the user to find the resposibilities of
     * @return all projects with a resposibility containing the given user
     */
    @SecureResult
    List<Project> findByResponsible(User user);

    /**
     * Takes responsibility for the given project on behalf of the given user.
     * No other user can already be responsible.
     *
     * @param project the project to take responsibility for
     * @param user    the user taking responsibility
     * @param comment an optional comment from the user describing why they are
     *                responsible/what they are fixing
     */
    @SecureParameter(parameterType = Project.class, action = ProjectConfigurationActions.ACTION_TAKE_RESPONSIBILITY)
    void takeResponsibility(Project project, User user, String comment);

    /**
     * Clears responsibility for the given project if it is set.  Only the
     * responsible user can do this, or an administrator.
     *
     * @param project the project to clear responsibility for
     */
    @SecureParameter(parameterType = Project.class, action = ProjectConfigurationActions.ACTION_CLEAR_RESPONSIBILITY)
    void clearResponsibility(Project project);

    /**
     * Returns all projects that are immediate downstream dependents of the
     * given project.  That is, all returned projects depend directly on the
     * given project.
     *
     * @param projectConfig project that the returned projects depend on
     * @return all projects that depend directly on the given project
     */
    @SecureResult
    List<ProjectConfiguration> getDownstreamDependencies(ProjectConfiguration projectConfig);

    /**
     * Retrieve a map of latest built revisions for each project.  If a project has not been
     * built, then it will contain a null revision.
     *
     * @return a map of project id to latest built revision.
     */
    Map<Long, Revision> getLatestBuiltRevisions();
}
