/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.api;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.*;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class RemoteApi
{
    private TokenManager tokenManager;
    private ShutdownManager shutdownManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private EventManager eventManager;

    public RemoteApi()
    {
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        ComponentContext.autowire(this);
    }

    public int getVersion()
    {
        Version v = Version.getVersion();
        return v.getIntBuildNumber();
    }

    public String login(String username, String password) throws AuthenticationException
    {
        return tokenManager.login(username, password);
    }

    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    public Vector<String> getAllProjectNames(String token) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        List<Project> projects = projectManager.getAllProjects();
        Vector<String> result = new Vector<String>(projects.size());
        for(Project p: projects)
        {
            result.add(p.getName());
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String token, String projectName, String buildSpecification, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = getProject(projectName);

        String[] specs = null;
        if(TextUtils.stringSet(buildSpecification))
        {
            specs = new String[] { buildSpecification };
        }

        ResultState[] states = null;
        if(completedOnly)
        {
            states = new ResultState[] { ResultState.ERROR, ResultState.FAILURE, ResultState.SUCCESS };
        }

        List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, specs, -1, -1, null, 0, maxResults, true);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for(BuildResult build: builds)
        {
            Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
            buildDetails.put("id", (int)build.getNumber());
            buildDetails.put("specification", build.getBuildSpecification());
            buildDetails.put("status", build.getState().getPrettyString());
            buildDetails.put("completed", build.completed());
            buildDetails.put("succeeded", build.succeeded());
            buildDetails.put("commenced", new Date(build.getStamps().getStartTime()));
            buildDetails.put("completed", new Date(build.getStamps().getEndTime()));
            result.add(buildDetails);
        }

        return result;
    }

    public Hashtable<String, Object> getLatestBuildForProject(String token, String projectName, String buildSpecification, boolean completedOnly) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> latest = getLatestBuildsForProject(token, projectName, buildSpecification, completedOnly, 1);
        if(latest.size() > 0)
        {
            return latest.get(0);
        }
        else
        {
            return null;
        }
    }

    public boolean triggerBuild(String token, String projectName, String buildSpecification) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = getProject(projectName);
            BuildSpecification spec = project.getBuildSpecification(buildSpecification);
            if(spec == null)
            {
                throw new IllegalArgumentException("Unknown build specification '" + buildSpecification + "'");
            }

            BuildRequestEvent event = new BuildRequestEvent(this, project, buildSpecification);
            eventManager.publish(event);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String getProjectState(String token, String projectName) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = getProject(projectName);
        return project.getState().toString().toLowerCase();
    }

    public boolean pauseProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = getProject(projectName);
            if(project.isPaused())
            {
                return false;
            }
            else
            {
                projectManager.pauseProject(project);
                return true;
            }
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean resumeProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = getProject(projectName);
            if(project.isPaused())
            {
                projectManager.resumeProject(project);
                return true;
            }
            else
            {
                return false;
            }
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean shutdown(String token, boolean force) throws AuthenticationException
    {
        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        tokenManager.verifyAdmin(token);

        ShutdownRunner runner = new ShutdownRunner(force);
        new Thread(runner).start();
        return true;
    }

    /**
     * Update the specified users password.
     *
     * @param token used to authenticate the request.
     *
     * @param login name identifying the user whose password is being set.
     * @param password is the new password.
     *
     * @return true if the request was successful, false otherwise.
     *
     * @throws AuthenticationException if the token does not authorise administrator access.
     */
    public boolean setPassword(String token, String login, String password) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(login);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown username '"+login +"'");
        }
        userManager.setPassword(user, password);
        userManager.save(user);
        return true;
    }

    /**
     * Required resource.
     *
     * @param tokenManager
     */
    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    /**
     * Required resource.
     *
     * @param shutdownManager
     */
    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    private Project getProject(String projectName)
    {
        Project project = projectManager.getProject(projectName);
        if(project == null)
        {
            throw new IllegalArgumentException("Unknown project '" + projectName + "'");
        }
        return project;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    private class ShutdownRunner implements Runnable
    {
        private boolean force;

        public ShutdownRunner(boolean force)
        {
            this.force = force;
        }

        public void run()
        {
            // Oh my, is this ever dodgy...
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // Empty
            }
            shutdownManager.shutdown(force);
        }
    }
}
