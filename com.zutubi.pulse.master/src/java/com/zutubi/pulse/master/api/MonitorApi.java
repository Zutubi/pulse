package com.zutubi.pulse.master.api;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.xwork.actions.project.ProjectHealth;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.util.EnumUtils;

import java.util.*;

/**
 * Implements an XML-RPC API with monitoring-specific functionality.  This is
 * intended for clients like Stethoscope that perform long-running monitoring
 * of a Pulse server.
 *
 * Accepts authentication tokens from the main {@link RemoteApi}.
 */
public class MonitorApi
{
    private TokenManager tokenManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    public MonitorApi()
    {
    }

    /**
     * @internal Retrieves the current status of all projects.
     *
     * @param token           authentication token
     * @param includePersonal if true, also include personal builds in the
     *                        result
     * @param lastTimestamp   if not empty, the timestamp from the last status
     *                        update - this is used to calculate builds that
     *                        have been completed since that update, so no
     *                        builds are missed
     * @return {@xtype RemoteApi.ProjectStatuses} a struct with a timestamp for
     *         this update and an array of project statuses
     */
    public Hashtable<String, Object> getStatusForAllProjects(String token, boolean includePersonal, String lastTimestamp)
    {
        User user = tokenManager.loginAndReturnUser(token);
        try
        {
            return getStatusForProjects(projectManager.getProjects(false), user, includePersonal, lastTimestamp);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * @internal Retrieves the current status of the logged-in user's
     * "my projects" set.
     *
     * @param token           authentication token
     * @param includePersonal if true, also include personal builds in the
     *                        result
     * @param lastTimestamp   if not empty, the timestamp from the last status
     *                        update - this is used to calculate builds that
     *                        have been completed since that update, so no
     *                        builds are missed
     * @return {@xtype RemoteApi.ProjectStatuses} a struct with a timestamp for
     *         this update and an array of project statuses
     */
    public Hashtable<String, Object> getStatusForMyProjects(String token, boolean includePersonal, String lastTimestamp)
    {
        User user = tokenManager.loginAndReturnUser(token);
        try
        {
            List<Project> projects = new LinkedList<Project>();
            if (user != null)
            {
                projects.addAll(userManager.getUserProjects(user, projectManager));
            }

            return getStatusForProjects(projects, user, includePersonal, lastTimestamp);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * @internal Retrieves the current status of the given project set.
     *
     * @param token           authentication token
     * @param projects        names of projects to get the status of
     * @param includePersonal if true, also include personal builds in the
     *                        result
     * @param lastTimestamp   if not empty, the timestamp from the last status
     *                        update - this is used to calculate builds that
     *                        have been completed since that update, so no
     *                        builds are missed
     * @return {@xtype RemoteApi.ProjectStatuses} a struct with a timestamp for
     *         this update and an array of project statuses
     */
    public Hashtable<String, Object> getStatusForProjects(String token, Vector<String> projects, boolean includePersonal, String lastTimestamp)
    {
        User user = tokenManager.loginAndReturnUser(token);
        try
        {
            List<Project> resolvedProjects = new LinkedList<Project>();
            for (String projectName: projects)
            {
                Project project = projectManager.getProject(projectName, false);
                if (project != null)
                {
                    resolvedProjects.add(project);
                }
            }

            return getStatusForProjects(resolvedProjects, user, includePersonal, lastTimestamp);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Hashtable<String, Object> getStatusForProjects(List<Project> projects, User user, boolean includePersonal, String lastTimestamp)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("timestamp", Long.toString(System.currentTimeMillis()));

        // Gather the in progress and completed since builds for all projects
        // at once to reduce the number of queries required.
        Project[] projectsArray = projects.toArray(new Project[projects.size()]);
        List<BuildResult> inProgress = buildManager.queryBuilds(projectsArray, ResultState.getIncompleteStates(), -1, -1, -1, -1, true);
        List<BuildResult> completedSince;
        long sinceTime = -1;
        if (lastTimestamp.length() == 0)
        {
            completedSince = Collections.emptyList();
        }
        else
        {
            try
            {
                sinceTime = Long.parseLong(lastTimestamp);
                completedSince = buildManager.getBuildsCompletedSince(projectsArray, sinceTime);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Invalid last timestamp '" + lastTimestamp + "'");
            }
        }

        Vector<Hashtable<String, Object>> projectStatuses = new Vector<Hashtable<String, Object>>();
        for (Project project: projects)
        {
            projectStatuses.add(getProjectStatus(project, inProgress, completedSince));
        }

        result.put("projects", projectStatuses);

        if(includePersonal)
        {
            result.put("personal", getPersonalBuildStatus(user, sinceTime));
        }

        return result;
    }

    private Hashtable<String, Object> getProjectStatus(final Project project, List<BuildResult> inProgress, List<BuildResult> completedSince)
    {
        Iterable<BuildResult> projectInProgress = Iterables.filter(inProgress, new Predicate<BuildResult>()
        {
            public boolean apply(BuildResult buildResult)
            {
                return buildResult.getProject().equals(project);
            }
        });

        final BuildResult latestComplete = buildManager.getLatestCompletedBuildResult(project);
        Iterable<BuildResult> projectCompletedSince;
        if (latestComplete == null)
        {
            projectCompletedSince = Collections.emptyList();
        }
        else
        {
            projectCompletedSince = Iterables.filter(completedSince, new Predicate<BuildResult>()
            {
                public boolean apply(BuildResult buildResult)
                {
                    return buildResult.getProject().equals(project) && !buildResult.equals(latestComplete);
                }
            });
        }

        return createStatus(project, projectInProgress, latestComplete, projectCompletedSince);
    }

    private Hashtable<String, Object> getPersonalBuildStatus(User user, long sinceTime)
    {
        List<BuildResult> personalBuilds = buildManager.getPersonalBuilds(user);
        List<BuildResult> inProgress = new LinkedList<BuildResult>();
        BuildResult latestCompleted = null;
        List<BuildResult> completedSince = new LinkedList<BuildResult>();

        for (BuildResult build: personalBuilds)
        {
            if (build.running())
            {
                inProgress.add(build);
            }
            else
            {
                if (latestCompleted == null)
                {
                    latestCompleted = build;
                }
                else
                {
                    if (build.getStamps().getEndTime() > sinceTime)
                    {
                        completedSince.add(build);
                    }
                }
            }
        }

        return createStatus(user, inProgress, latestCompleted, completedSince);
    }

    private Hashtable<String, Object> createStatus(NamedEntity owner, Iterable<BuildResult> inProgress, BuildResult latestComplete, Iterable<BuildResult> completedSince)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("owner", owner.getName());
        result.put("health", EnumUtils.toPrettyString(ProjectHealth.getHealth(latestComplete)));
        if (latestComplete != null)
        {
            result.put("latestCompleted", ApiUtils.convertBuild(latestComplete, false));
        }

        result.put("inProgress", ApiUtils.mapBuilds(inProgress, false));
        result.put("completedSince", ApiUtils.mapBuilds(completedSince, false));
        return result;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                // Rewire on startup to get the full token manager.
                SpringComponentContext.autowire(MonitorApi.this);
            }
        });
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }
}