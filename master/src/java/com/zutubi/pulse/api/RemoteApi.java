package com.zutubi.pulse.api;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scm.SCMConfiguration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.validation.PulseValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.*;

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
    private AgentManager agentManager;

    private ValidationManager validationManager;

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
        for (User user : users)
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
        for (Project p : projects)
        {
            result.add(p.getName());
        }

        return result;
    }

    public Vector<String> getMyProjectNames(String token) throws AuthenticationException
    {
        User user = tokenManager.verifyUser(token);
        List<Project> projects = projectManager.getAllProjects();

        if (user != null)
        {
            projects.removeAll(userManager.getHiddenProjects(user));
        }

        Vector<String> result = new Vector<String>(projects.size());
        for (Project p : projects)
        {
            result.add(p.getName());
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getBuild(String token, String projectName, int id) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);
        BuildResult build = buildManager.getByProjectAndNumber(project, id);
        if (build == null)
        {
            return result;
        }

        result.add(convertResult(build));
        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String token, String projectName, String buildSpecification, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        tokenManager.verifyUser(token);
        Project project = internalGetProject(projectName);

        String[] specs = null;
        if (TextUtils.stringSet(buildSpecification))
        {
            specs = new String[]{buildSpecification};
        }

        ResultState[] states = null;
        if (completedOnly)
        {
            states = ResultState.getCompletedStates();
        }

        List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, specs, -1, -1, null, 0, maxResults, true);
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestBuildForProject(String token, String projectName, String buildSpecification, boolean completedOnly) throws AuthenticationException
    {
        return getLatestBuildsForProject(token, projectName, buildSpecification, completedOnly, 1);
    }

    public Vector<Hashtable<String, Object>> getPersonalBuild(String token, int id) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        User user = tokenManager.verifyUser(token);
        BuildResult build = buildManager.getByUserAndNumber(user, id);
        if (build == null)
        {
            return result;
        }

        result.add(convertResult(build));
        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuilds(String token, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        User user = tokenManager.verifyUser(token);

        List<BuildResult> builds = buildManager.getPersonalBuilds(user);
        if (completedOnly)
        {
            Iterator<BuildResult> it = builds.iterator();
            while (it.hasNext())
            {
                BuildResult b = it.next();
                if (!b.completed())
                {
                    it.remove();
                }
            }
        }

        if (maxResults >= 0 && builds.size() > maxResults)
        {
            builds = builds.subList(0, maxResults);
        }

        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
        for (BuildResult build : builds)
        {
            Hashtable<String, Object> buildDetails = convertResult(build);
            result.add(buildDetails);
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuild(String token, boolean completedOnly) throws AuthenticationException
    {
        return getLatestPersonalBuilds(token, completedOnly, 1);
    }

    private Hashtable<String, Object> convertResult(BuildResult build)
    {
        Hashtable<String, Object> buildDetails = new Hashtable<String, Object>();
        buildDetails.put("id", (int) build.getNumber());
        buildDetails.put("project", build.getProject().getName());
        buildDetails.put("specification", build.getBuildSpecification());
        buildDetails.put("status", build.getState().getPrettyString());
        buildDetails.put("completed", build.completed());
        buildDetails.put("succeeded", build.succeeded());

        TimeStamps timeStamps = build.getStamps();
        buildDetails.put("startTime", new Date(timeStamps.getStartTime()));
        buildDetails.put("endTime", new Date(timeStamps.getEndTime()));
        if (timeStamps.hasEstimatedTimeRemaining())
        {
            buildDetails.put("progress", timeStamps.getEstimatedPercentComplete());
        }
        else
        {
            buildDetails.put("progress", -1);
        }

        return buildDetails;
    }

    public boolean triggerBuild(String token, String projectName, String buildSpecification) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName);
            getBuildSpecification(project, buildSpecification);
            projectManager.triggerBuild(project, buildSpecification, new RemoteTriggerBuildReason(), null, true);
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
        Project project = internalGetProject(projectName);
        return project.getState().toString().toLowerCase();
    }

    public Hashtable<String, String> preparePersonalBuild(String token, String projectName, String buildSpecification) throws AuthenticationException
    {
        tokenManager.verifyRoleIn(token, GrantedAuthority.PERSONAL);
        Project project = internalGetProject(projectName);
        getBuildSpecification(project, buildSpecification);

        Hashtable<String, String> scmDetails = new Hashtable<String, String>();
        scmDetails.put(SCMConfiguration.PROPERTY_TYPE, project.getScm().getType());
        scmDetails.putAll(project.getScm().getRepositoryProperties());
        return scmDetails;
    }

    public boolean pauseProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName);
            if (project.isPaused())
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
            Project project = internalGetProject(projectName);
            if (project.isPaused())
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

    public boolean addAgent(String token, String name, String host, int port) throws AuthenticationException, LicenseException
    {
        tokenManager.verifyAdmin(token);
        LicenseHolder.ensureAuthorization(LicenseHolder.AUTH_ADD_AGENT);

        if (!TextUtils.stringSet(name) || agentManager.agentExists(name))
        {
            return false;
        }

        if (!TextUtils.stringSet(host))
        {
            return false;
        }

        if (port <= 0)
        {
            return false;
        }

        agentManager.addSlave(new Slave(name, host, port));
        return true;
    }

    public String getAgentStatus(String token, String name) throws AuthenticationException
    {
        tokenManager.verifyUser(token);

        Agent agent = agentManager.getAgent(name);
        if (agent == null)
        {
            return "";
        }

        return agent.getStatus().getPrettyString();
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
            if (!adminTokenManager.checkAdminToken(token))
            {
                throw new AuthenticationException("Invalid token");
            }
        }

        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    public boolean stopService(String token) throws AuthenticationException
    {
        if (tokenManager != null)
        {
            tokenManager.verifyAdmin(token);
        }
        else
        {
            if (!adminTokenManager.checkAdminToken(token))
            {
                throw new AuthenticationException("Invalid token");
            }
        }

        shutdownManager.delayedStop();
        return true;
    }

    /**
     * Updates the specified users password.
     *
     * @param token    used to authenticate the request.
     * @param login    name identifying the user whose password is being set.
     * @param password is the new password.
     * @return true if the request was successful, false otherwise.
     * @throws AuthenticationException if the token does not authorise administrator access.
     */
    public boolean setPassword(String token, String login, String password) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(login);
        if (user == null)
        {
            throw new IllegalArgumentException("Unknown username '" + login + "'");
        }
        userManager.setPassword(user, password);
        userManager.save(user);
        return true;
    }

    /**
     * Create a new user.
     *
     * @param user      is a map of containing the users details.
     * @param token     used to authenticate the request
     *
     * @return true if the request is successful
     *
     * @throws AuthenticationException if you are not authorised to execute this action.
     * @throws LicenseException        if you are not licensed to execute this action.
     */
    public boolean createUser(String token, Hashtable<String, Object> user) throws AuthenticationException, LicenseException
    {
        tokenManager.verifyAdmin(token);
        LicenseHolder.ensureAuthorization(LicenseHolder.AUTH_ADD_USER);

        // validate the user details.
        User existingUser = userManager.getUser((String) user.get("login"));
        if (existingUser != null)
        {
            return false;
        }

        User instance = new User();
        instance.setLogin((String) user.get("login"));
        instance.setName((String) user.get("name"));

        userManager.save(instance);

        userManager.setPassword(instance, (String) user.get("password"));

        return true;
    }

    /**
     * Delete the specified user.
     *
     * @param token used to authenticate the request.
     * @param login identifies the user to be deleted.
     * @return true if the request is successful, false otherwise.
     * @throws AuthenticationException is you are not authorised to execute this request.
     *
     * @throws ValidationException if no user with the specified login exists.
     */
    public boolean deleteUser(String token, String login) throws AuthenticationException, ValidationException
    {
        tokenManager.verifyAdmin(token);

        User user = userManager.getUser(login);
        if (user == null)
        {
            throw new ValidationException(String.format("Unknown user login: '%s'", login));
        }
        userManager.delete(user);
        return true;
    }

    /**
     * Create a new project.
     *
     * @param token     used to authenticate the request.
     * @param project   the project details
     * @param scm       the scm details
     * @param type      the project type details
     *
     * @return true if the request is successful, false otherwise.
     * 
     * @throws AuthenticationException  if you are not authorised to execute this action.
     * @throws LicenseException         if you are not licensed to execute this action.
     * @throws ValidationException      if a validation error is detected.
     */
    public boolean createProject(String token, Hashtable<String, Object> project, Hashtable<String, Object> scm, Hashtable<String, Object> type) throws AuthenticationException, LicenseException, ValidationException
    {
        User user = tokenManager.verifyAdmin(token);
        try
        {
            AcegiUtils.loginAs(user);

            Project existingProject = projectManager.getProject((String) project.get("name"));
            if (existingProject != null)
            {
                return false;
            }

            Project newProject = new Project();
            setProperties(project, newProject);
            validate(newProject);

            // lookup scm.
            Scm newScm = createScm(scm);
            validate(newScm);
            newProject.setScm(newScm);

            // set the details.
            PulseFileDetails newType = createFileDetails(type);
            validate(newType);
            newProject.setPulseFileDetails(newType);

            // set the details.
            projectManager.create(newProject);

            return true;
        }
        finally
        {
            AcegiUtils.logout();
        }
    }

    /**
     * Delete the specified project.
     *
     * @param token used to authenticate the request.
     * @param name  the name of the project to be deleted.
     * @return true if the request is successful, false otherwise.
     *
     * @throws AuthenticationException if you are not authorised to execute this request.
     *
     * @throws ValidationException if no project with the specified name exists.
     */
    public boolean deleteProject(String token, String name) throws AuthenticationException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new ValidationException(String.format("Unknown project name: '%s'", name));
            }

            projectManager.delete(project);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean editProject(String token, String name, Hashtable<String, Object> projectDetails) throws AuthenticationException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new ValidationException(String.format("Unknown project name: '%s'", name));
            }
            
            // are we changing the name of the project? if so, then we need to check that the new name is not already in use.
            if (projectDetails.containsKey("name"))
            {
                String newName = (String) projectDetails.get("name");
                if (!name.equals(newName) && projectManager.getProject(newName) != null)
                {
                    throw new ValidationException(String.format("The name '%s' is already in use by another project. Please select a different name.", newName));
                }
            }

            setProperties(projectDetails, project);
            validate(project);

            projectManager.save(project);

            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Get the details for the specified project.
     *
     * @param name is the name of the project to be retrieved.
     * @param token used to authenticate the request.
     *
     * @return a mapping of the projects details.
     *
     * @throws IllegalArgumentException if the specified name does not reference a project.
     * @throws AuthenticationException if you are not authorised to execute this request.
     */
    public Hashtable<String, Object> getProject(String token, String name) throws IllegalArgumentException, AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = projectManager.getProject(name);
            if (project == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project name: '%s'", name));
            }

            Hashtable<String, Object> details = new Hashtable<String, Object>();
            details.put("name", project.getName());
            details.put("description", emptyStringIfNull(project.getDescription()));
            details.put("url", emptyStringIfNull(project.getUrl()));

            return details;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Object emptyStringIfNull(Object obj)
    {
        if (obj != null)
        {
            return obj;
        }
        return "";
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
        for (CommitMessageTransformer t : transformers)
        {
            projectManager.delete(t);
        }
        return result;
    }

    private void validate(Object o) throws ValidationException
    {
        ValidationContext ctx = new PulseValidationContext(o);
        validationManager.validate(o, ctx);
        if (ctx.hasErrors())
        {
            if (ctx.hasFieldErrors())
            {
                String field = ctx.getFieldErrors().keySet().iterator().next();
                String message = ctx.getFieldErrors(field).iterator().next();
                throw new ValidationException(String.format("Field %s is invalid. Reason: %s", field, message));
            }
            if (ctx.hasActionErrors())
            {
                String message = ctx.getActionErrors().iterator().next();
                throw new ValidationException(String.format("The following error occured validating your request: %s", message));
            }
        }
    }

    private PulseFileDetails createFileDetails(Hashtable<String, Object> type) throws ValidationException
    {
        //TODO: This goes into the project type manager.
        String projectType = (String) type.remove("type");

        PulseFileDetails details;
        if ("ant".equals(projectType))
        {
            details = new AntPulseFileDetails();
        }
        else if ("maven".equals(projectType))
        {
            details = new MavenPulseFileDetails();
        }
        else if ("maven2".equals(projectType))
        {
            details = new Maven2PulseFileDetails();
        }
        else if ("xcode".equals(projectType))
        {
            details = new XCodePulseFileDetails();
        }
        else if ("custom".equals(projectType))
        {
            details = new CustomPulseFileDetails();
        }
        else if ("versioned".equals(projectType))
        {
            details = new VersionedPulseFileDetails();
        }
        else
        {
            throw new ValidationException("Unknown project type: " + type);
        }
        setProperties(type, details);
        return details;
    }

    private Scm createScm(Hashtable<String, Object> details) throws ValidationException
    {
        //TODO: This goes into the ScmManager.
        String type = (String) details.remove("type");

        Scm scm;
        if ("cvs".equals(type))
        {
            scm = new Cvs();
        }
        else if ("svn".equals(type))
        {
            scm = new Svn();
        }
        else if ("p4".equals(type))
        {
            scm = new P4();
        }
        else
        {
            throw new ValidationException("Unknown scm type: " + type);
        }

        setProperties(details, scm);

        return scm;
    }

    private void setProperties(Hashtable<String, Object> scmDetails, Object object)
    {
        Enumeration<String> keys = scmDetails.keys();
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            try
            {
                Ognl.setValue(key, object, scmDetails.get(key));
            }
            catch (OgnlException e)
            {
                throw new IllegalArgumentException(String.format("Failed to set '%s' on object '%s'. Cause: %s", key, object, e.getMessage()));
            }
        }
    }

    private Project internalGetProject(String projectName)
    {
        Project project = projectManager.getProject(projectName);
        if (project == null)
        {
            throw new IllegalArgumentException("Unknown project '" + projectName + "'");
        }
        return project;
    }

    private BuildSpecification getBuildSpecification(Project project, String buildSpecification)
    {
        BuildSpecification spec = project.getBuildSpecification(buildSpecification);
        if (spec == null)
        {
            throw new IllegalArgumentException("Unknown build specification '" + buildSpecification + "'");
        }

        return spec;
    }

    /**
     * Required resource.
     *
     * @param tokenManager instance
     */
    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    /**
     * Required resource.
     *
     * @param shutdownManager instance
     */
    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    /**
     * Required resource.
     *
     * @param userManager instance
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

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.validationManager = validationManager;
    }
}
