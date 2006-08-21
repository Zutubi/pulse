package com.zutubi.pulse.api;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.ResultState;
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
    private AdminTokenManager adminTokenManager;
    private TokenManager tokenManager;

    private ShutdownManager shutdownManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    public RemoteApi()
    {
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        ComponentContext.autowire(this);
    }

    public int getVersion()
    {
        Version v = Version.getVersion();
        return v.getBuildNumberAsInt();
    }

    public String login(String username, String password) throws AuthenticationException
    {
        return tokenManager.login(username, password);
    }

    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    public String ping()
    {
        return "pong";
    }

    public Vector<String> getAllUserLogins(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        List<User> users = userManager.getAllUsers();
        Vector<String> result = new Vector<String>(users.size());
        for(User user: users)
        {
            result.add(user.getLogin());
        }

        return result;
    }

    public Vector<String> getAllProjectNames(String token) throws AuthenticationException
    {
        //@Secured({"ROLE_USER"})
        tokenManager.verifyUser(token);

        List<Project> projects = projectManager.getAllProjects();
        Vector<String> result = new Vector<String>(projects.size());
        for(Project p: projects)
        {
            result.add(p.getName());
        }

        return result;
    }

    public Vector<String> getMyProjectNames(String token) throws AuthenticationException
    {
        User user = tokenManager.verifyUser(token);
        List<Project> projects = projectManager.getAllProjects();

        if(user != null)
        {
            projects.removeAll(userManager.getHiddenProjects(user));
        }
        
        Vector<String> result = new Vector<String>(projects.size());
        for(Project p: projects)
        {
            result.add(p.getName());
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getBuild(String token, String projectName, int id) throws AuthenticationException
    {
        Vector<Hashtable<String,  Object>> result = new Vector<Hashtable<String, Object>>(1);

        tokenManager.verifyUser(token);
        Project project = getProject(projectName);
        BuildResult build = buildManager.getByProjectAndNumber(project, id);
        if(build == null)
        {
            return result;
        }

        result.add(convertResult(build));
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
            states = ResultState.getCompletedStates();
        }

        List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, specs, -1, -1, null, 0, maxResults, true);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for(BuildResult build: builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String,Object>> getLatestBuildForProject(String token, String projectName, String buildSpecification, boolean completedOnly) throws AuthenticationException
    {
        return getLatestBuildsForProject(token, projectName, buildSpecification, completedOnly, 1);
    }

    private Hashtable<String, Object> convertResult(BuildResult build)
    {
        Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
        buildDetails.put("id", (int)build.getNumber());
        buildDetails.put("specification", build.getBuildSpecification());
        buildDetails.put("status", build.getState().getPrettyString());
        buildDetails.put("completed", build.completed());
        buildDetails.put("succeeded", build.succeeded());
        buildDetails.put("startTime", new Date(build.getStamps().getStartTime()));
        buildDetails.put("endTime", new Date(build.getStamps().getEndTime()));
        return buildDetails;
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

            projectManager.triggerBuild(project, buildSpecification, new RemoteTriggerBuildReason(), new BuildRevision(), true);
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

    public boolean shutdown(String token, boolean force, boolean exitJvm) throws AuthenticationException
    {
        // check the tokenmanager. If we have one, then lets us it. If not, then its very early in
        // the setup process, so fallback to the admin token manager.
        if (tokenManager != null)
        {
            tokenManager.verifyAdmin(token);
        }
        else
        {
            adminTokenManager.checkAdminToken(token);
        }

        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    /**
     * Updates the specified users password.
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
     * Deletes all commit message links, primarily for testing purposes.
     *
     * @param token used to authenticate the request
     * @return the number of commit message links deleted
     * @throws AuthenticationException if the token does not authorise administrator access
     */
    public int deleteAllCommitMessageLinks(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        List<CommitMessageTransformer> transformers = projectManager.getCommitMessageTransformers();
        int result = transformers.size();
        for(CommitMessageTransformer t: transformers)
        {
            projectManager.delete(t);
        }
        return result;
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

    public void setAdminTokenManager(AdminTokenManager adminTokenManager)
    {
        this.adminTokenManager = adminTokenManager;
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
}
