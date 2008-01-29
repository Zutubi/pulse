package com.zutubi.pulse.api;

import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import com.zutubi.pulse.ShutdownManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.scm.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.group.ServerPermission;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;
import org.acegisecurity.AccessDeniedException;

import java.util.*;

/**
 * Implements a simple API for remote monitoring and control.
 */
public class RemoteApi implements com.zutubi.pulse.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(RemoteApi.class);

    private TokenManager tokenManager;
    private EventManager eventManager;
    private ShutdownManager shutdownManager;
    private MasterConfigurationManager configurationManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private TypeRegistry typeRegistry;
    private RecordManager recordManager;

    private ActionManager actionManager;
    private AgentManager agentManager;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;
    private ScmClientFactory<ScmConfiguration> scmClientFactory;

    public RemoteApi()
    {
        // can remove this call when we sort out autowiring from the XmlRpcServlet.
        ComponentContext.autowire(this);
    }

    /**
     * @return the version of this Pulse installation as a build number.  The
     *         number is of the form:
     *         &lt;major&gt;&lt;minor&gt;&lt;build&gt;&lt;patch&gt;
     *         where &lt;major&gt; and &lt;minor&gt; are two digits and
     *         &lt;build&gt; and &lt;patch&gt; are three digits.  The value
     *         of &lt;patch&gt; will always be 000 in regular builds.  For
     *         example, version 2.0.12 would have build number 0200012000, so
     *         this method would return 200012000.
     */
    public int getVersion()
    {
        Version v = Version.getVersion();
        return v.getBuildNumberAsInt();
    }

    /**
     * Authenticates as the given user and returns a token that can be used
     * as credentials for other method calls.  The returned token is valid
     * for 30 minutes or until explicitly invalidated by a call to
     * {@link #logout(String)}.
     *
     * @see #logout(String)
     *
     * @param username login of the user to authenticate as
     * @param password password of the user
     * @return an authentication token that can be used as credentials for
     *         other method calls
     * @throws AuthenticationException if the user does not exist or the
     *         password does not match
     */
    public String login(String username, String password) throws AuthenticationException
    {
        return tokenManager.login(username, password);
    }

    /**
     * Explicitly invalidates the given authentication token, such that it
     * cannot be used for further method calls.
     *
     * @see #login(String, String)
     * 
     * @param token the token to invalidate
     * @return true if the given token was valid before this call
     */
    public boolean logout(String token)
    {
        return tokenManager.logout(token);
    }

    /**
     * A trivial ping method that can be useful for testing connectivity.
     *
     * @return the value "pong"
     */
    public String ping()
    {
        return "pong";
    }

    /**
     * Tests whether the given configuration path exists and is visible to
     * the logged in user.
     *
     * @param token authentication token (see {@link #login})
     * @param path the path to test, e.g. "projects/my project"
     * @return true iff the given configuration path exists and is visible to
     *         the logged in user
     * @throws AuthenticationException if the given token is invalid
     */
    public boolean configPathExists(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            return configurationTemplateManager.pathExists(path) && configurationSecurityManager.hasPermission(path, AccessManager.ACTION_VIEW);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Returns a list of sub paths that are nested under the given
     * configuration path.  For example, if the path "projects" is given, a
     * list of all project names will be returned. Paths not visible to the
     * logged in user are filtered out.
     *
     * @param token authentication token (see {@link #login})
     * @param path the path to list the sub paths of
     * @return all sub paths of the given path that are visible to the logged
     *         in user
     * @throws AuthenticationException if the given token in invalid
     * @throws IllegalArgumentException if the given path is invalid
     */
    public Vector<String> getConfigListing(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Vector<String> result;

            if (path.length() == 0)
            {
                result = new Vector<String>(configurationTemplateManager.getRootListing());
            }
            else
            {
                Type type = configurationTemplateManager.getType(path);
                if (type instanceof CollectionType)
                {
                    CollectionType collectionType = (CollectionType) type;
                    if (collectionType.getCollectionType() instanceof ComplexType)
                    {
                        Record record = configurationTemplateManager.getRecord(path);
                        result = new Vector<String>(collectionType.getOrder(record));
                    }
                    else
                    {
                        throw new IllegalArgumentException("Path refers to simple collection");
                    }
                }
                else
                {
                    CompositeType compositeType = (CompositeType) type;
                    result = new Vector<String>(compositeType.getNestedPropertyNames());
                }
            }

            configurationSecurityManager.filterPaths(path, result, AccessManager.ACTION_VIEW);
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    /**
     * Creates a default configuration object of the given type.  This object
     * will not necessarily be valid - some fields may be incomplete.
     *
     * @param token        authentication token (see {@link #login})
     * @param symbolicName symbolic name of the configuration type to create
     *                     an instance of, e.g. "zutubi.projectConfig"
     * @return a default configuration object of the given type
     * @throws AuthenticationException if the given token is invalid
     * @throws IllegalArgumentException if the given symbolic name is invalid
     * @throws TypeException if there is an error constructing the object
     */
    public Hashtable<String, Object> createDefaultConfig(String token, String symbolicName) throws AuthenticationException, TypeException
    {
        tokenManager.verifyUser(token);
        CompositeType type = typeRegistry.getType(symbolicName);
        if (type == null)
        {
            throw new IllegalArgumentException("Unrecognised symbolic name '" + symbolicName + "'");
        }

        MutableRecord record = type.createNewRecord(true);
        return type.toXmlRpc(record);
    }

    public Object getConfig(String token, String path) throws AuthenticationException, TypeException
    {
        tokenManager.loginUser(token);

        try
        {
            Object instance = configurationTemplateManager.getInstance(path);
            if (instance == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

            Type t = configurationTemplateManager.getType(path);
            return t.toXmlRpc(t.unstantiate(instance));
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String getConfigHandle(String token, String path) throws AuthenticationException, TypeException
    {
        tokenManager.loginUser(token);

        try
        {
            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

            Record record = configurationTemplateManager.getRecord(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            return Long.toString(record.getHandle());
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Object getRawConfig(String token, String path) throws AuthenticationException, TypeException
    {
        tokenManager.loginUser(token);
        try
        {
            Record record = recordManager.select(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);

            Type t = configurationTemplateManager.getType(path);
            return t.toXmlRpc(record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean isConfigValid(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationTemplateManager.getInstance(path);
            if(instance == null)
            {
                throw new IllegalArgumentException("Path '" + path + "' does not exist");
            }

            configurationSecurityManager.ensurePermission(path, AccessManager.ACTION_VIEW);
            return instance.isValid();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String insertConfig(String token, String path, Hashtable config) throws AuthenticationException, TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            ComplexType pathType = configurationTemplateManager.getType(path);
            CompositeType expectedType = (CompositeType) pathType.getTargetType();

            String parentPath;
            String baseName;
            if (pathType instanceof CollectionType)
            {
                parentPath = path;
                baseName = null;
            }
            else
            {
                parentPath = PathUtils.getParentPath(path);
                baseName = PathUtils.getBaseName(path);
            }

            if (configurationTemplateManager.isTemplatedCollection(parentPath))
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': use insertTemplatedConfig to insert into templated collections");
            }

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            CompositeType type = configurationTemplateManager.typeCheck(expectedType, symbolicName);
            MutableRecord record = type.fromXmlRpc(config);

            Configuration instance = configurationTemplateManager.validate(parentPath, baseName, record, true);
            if (!type.isValid(instance))
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.insertRecord(path, record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String insertTemplatedConfig(String token, String templateParentPath, Hashtable config, boolean template) throws AuthenticationException, TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            String insertPath = PathUtils.getParentPath(templateParentPath);
            if (insertPath == null)
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': no parent path");
            }

            if (!configurationTemplateManager.isTemplatedCollection(insertPath))
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': parent path '" + insertPath + "' is not a templated collection, use insertConfig instead");
            }

            TemplateRecord templateParent = (TemplateRecord) configurationTemplateManager.getRecord(templateParentPath);
            if (templateParent == null)
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': template parent does not exist");
            }

            if (configurationTemplateManager.isConcrete(insertPath, templateParent))
            {
                throw new IllegalArgumentException("Invalid templateParentPath '" + templateParentPath + "': template parent is concrete and thus cannot be inherited from");
            }

            CompositeType expectedType = (CompositeType) templateParent.getType();

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            CompositeType type = configurationTemplateManager.typeCheck(expectedType, symbolicName);
            MutableRecord record = type.fromXmlRpc(config);
            configurationTemplateManager.setParentTemplate(record, templateParent.getHandle());
            if (template)
            {
                configurationTemplateManager.markAsTemplate(record);
            }

            Configuration instance = configurationTemplateManager.validate(insertPath, null, record, true);
            if (!type.isValid(instance))
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.insertRecord(insertPath, record);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String saveConfig(String token, String path, Hashtable config, boolean deep) throws AuthenticationException, TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Record existingRecord = configurationTemplateManager.getRecord(path);
            if (existingRecord == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': no existing record found (use insert to create new records)");
            }

            String existingSymbolicName = existingRecord.getSymbolicName();
            if (existingSymbolicName == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': path refers to a collection (manipulate collections using insert and delete)");
            }

            String symbolicName = CompositeType.getTypeFromXmlRpc(config);
            if (!existingSymbolicName.equals(symbolicName))
            {
                throw new IllegalArgumentException("Expecting type '" + existingSymbolicName + "', found '" + symbolicName + "' (type cannot be changed by saving)");
            }

            CompositeType type = typeRegistry.getType(existingSymbolicName);
            MutableRecord record = type.fromXmlRpc(config);

            Configuration instance = configurationTemplateManager.validate(PathUtils.getParentPath(path), PathUtils.getBaseName(path), record, deep);
            if ((deep && !type.isValid(instance)) || !instance.isValid())
            {
                throw new ValidationException(type, instance);
            }

            return configurationTemplateManager.saveRecord(path, record, deep);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean deleteConfig(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            if (configurationTemplateManager.getRecord(path) == null)
            {
                return false;
            }

            configurationTemplateManager.delete(path);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public int deleteAllConfigs(String token, String pathPattern) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            return configurationTemplateManager.deleteAll(pathPattern);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean restoreConfig(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            configurationTemplateManager.restore(path);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean setConfigOrder(String token, String path, Vector<String> order) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            configurationTemplateManager.setOrder(path, order);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getConfigActions(String token, String path) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationTemplateManager.getInstance(path);
            return new Vector<String>(actionManager.getActions(instance, false));
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean doConfigAction(String token, String path, String action) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationTemplateManager.getInstance(path);
            actionManager.execute(action, instance, null);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }

    }

    public boolean doConfigActionWithArgument(String token, String path, String action, Hashtable argument) throws AuthenticationException, TypeException, ValidationException
    {
        tokenManager.loginUser(token);
        try
        {
            Configuration instance = configurationTemplateManager.getInstance(path);

            String symbolicName = CompositeType.getTypeFromXmlRpc(argument);
            CompositeType type = typeRegistry.getType(symbolicName);
            MutableRecord record = type.fromXmlRpc(argument);
            Configuration arg = configurationTemplateManager.validate(null, null, record, true);
            if(!type.isValid(arg))
            {
                throw new ValidationException(type, arg);
            }


            actionManager.execute(action, instance, arg);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }

    }

    public boolean logError(String token, String message) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        LOG.severe(message);
        return true;
    }

    public boolean logWarning(String token, String message) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        LOG.warning(message);
        return true;
    }

    public Vector<String> getAllUserLogins(String token) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Collection<User> users = userManager.getAllUsers();
            Vector<String> result = new Vector<String>(users.size());
            for (User user : users)
            {
                result.add(user.getConfig().getLogin());
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllProjectNames(String token) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            List<Project> projects = projectManager.getProjects(true);
            return getNames(projects);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getMyProjectNames(String token) throws AuthenticationException
    {
        User user = tokenManager.loginUser(token);
        try
        {
            List<Project> projects = new LinkedList<Project>();
            if (user != null)
            {
                projects.addAll(userManager.getUserProjects(user, projectManager));
            }

            return getNames(projects);
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<String> getAllProjectGroups(String token) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Collection<ProjectGroup> groups = projectManager.getAllProjectGroups();
            Vector<String> result = new Vector<String>(groups.size());
            for (ProjectGroup g : groups)
            {
                result.add(g.getName());
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Hashtable<String, Object> getProjectGroup(String token, String name) throws AuthenticationException, IllegalArgumentException
    {
        tokenManager.loginUser(token);
        try
        {
            ProjectGroup group = projectManager.getProjectGroup(name);
            if(group == null)
            {
                throw new IllegalArgumentException(String.format("Unknown project group: '%s'", name));
            }

            Hashtable<String, Object> result = new Hashtable<String, Object>();
            result.put("name", group.getName());
            result.put("projects", getNames(group.getProjects()));
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Vector<String> getNames(Collection<Project> projects)
    {
        Vector<String> result = new Vector<String>(projects.size());
        CollectionUtils.map(projects, new Mapping<Project, String>()
        {
            public String map(Project project)
            {
                return project.getName();
            }
        }, result);

        return result;
    }

    public Vector<String> getAllAgentNames(String token) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            List<Agent> agents = agentManager.getAllAgents();
            Vector<String> result = new Vector<String>(agents.size());
            CollectionUtils.map(agents, new Mapping<Agent, String>()
            {
                public String map(Agent agent)
                {
                    return agent.getConfig().getName();
                }
            }, result);

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);
            Project project = internalGetProject(projectName, true);
            BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                return result;
            }

            result.add(convertResult(build));
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean deleteBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            projectManager.checkWrite(project);
            BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                return false;
            }

            buildManager.delete(build);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> queryBuildsForProject(String token, String projectName, String[] resultStates, int firstResult, int maxResults, boolean mostRecentFirst) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);

            List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, mapStates(resultStates), -1, -1, null, firstResult, maxResults, mostRecentFirst);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
            for (BuildResult build : builds)
            {
                Hashtable<String, Object> buildDetails = convertResult(build);
                result.add(buildDetails);
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getBuildRange(String token, String projectName, int afterBuild, int toBuild) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            List<BuildResult> buildRange = buildManager.queryBuilds(project, ResultState.getCompletedStates(), afterBuild + 1, toBuild, 0, -1, false, false);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(buildRange.size());
            for(BuildResult r: buildRange)
            {
                result.add(convertResult(r));
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getPreviousBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            BuildResult buildResult = internalGetBuild(project, id);
            buildResult = buildManager.getPreviousBuildResult(buildResult);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();
            if(buildResult != null)
            {
                result.add(convertResult(buildResult));
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private ResultState[] mapStates(String[] stateNames)
    {
        if(stateNames.length > 0)
        {
            ResultState[] states = new ResultState[stateNames.length];
            for(int i = 0; i < stateNames.length; i++)
            {
                states[i] = ResultState.fromPrettyString(stateNames[i]);
            }

            return states;
        }
        else
        {
            return null;
        }
    }

    public int getNextBuildNumber(String token, String projectName) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            return (int) project.getNextBuildNumber();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String token, String projectName, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);

            ResultState[] states = null;
            if (completedOnly)
            {
                states = ResultState.getCompletedStates();
            }

            List<BuildResult> builds = buildManager.queryBuilds(new Project[]{project}, states, -1, -1, null, 0, maxResults, true);
            Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(builds.size());
            for (BuildResult build : builds)
            {
                Hashtable<String, Object> buildDetails = convertResult(build);
                result.add(buildDetails);
            }

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestBuildForProject(String token, String projectName, boolean completedOnly) throws AuthenticationException
    {
        return getLatestBuildsForProject(token, projectName, completedOnly, 1);
    }

    public Vector<Hashtable<String, Object>> getPersonalBuild(String token, int id) throws AuthenticationException
    {
        Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>(1);

        User user = tokenManager.loginUser(token);
        try
        {
            BuildResult build = buildManager.getByUserAndNumber(user, id);
            if (build == null)
            {
                return result;
            }

            result.add(convertResult(build));
            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, Object>> getLatestPersonalBuilds(String token, boolean completedOnly, int maxResults) throws AuthenticationException
    {
        User user = tokenManager.loginUser(token);
        try
        {
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
        finally
        {
            tokenManager.logoutUser();
        }
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
        buildDetails.put("revision", getBuildRevision(build));
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

    private String getBuildRevision(BuildResult build)
 	{
        Revision revision = build.getRevision();
        if(revision != null)
        {
            return revision.getRevisionString();
        }

         return "";
 	}

    public Vector<Hashtable<String, Object>> getChangesInBuild(String token, String projectName, int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            final BuildResult build = buildManager.getByProjectAndNumber(project, id);
            if (build == null)
            {
                throw new IllegalArgumentException("Unknown build '" + id + "' for project '" + projectName + "'");
            }

            final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();
            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    List<Changelist> changelists = buildManager.getChangesForBuild(build);
                    for(Changelist change: changelists)
                    {
                        result.add(convertChangelist(change));
                    }
                }
            });

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Hashtable<String, Object> convertChangelist(Changelist change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getRevision() != null && change.getRevision().getRevisionString() != null)
        {
            result.put("revision", change.getRevision().getRevisionString());
        }
        if(change.getUser() != null)
        {
            result.put("author", change.getUser());
        }
        if(change.getDate() != null)
        {
            result.put("date", change.getDate());
        }
        if(change.getComment() != null)
        {
            result.put("comment", change.getComment());
        }

        Vector<Hashtable<String, Object>> files = new Vector<Hashtable<String, Object>>(change.getChanges().size());
        for(Change file: change.getChanges())
        {
            files.add(convertChange(file));
        }
        result.put("files", files);

        return result;
    }

    private Hashtable<String, Object> convertChange(Change change)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        if(change.getFilename() != null)
        {
            result.put("file", change.getFilename());
        }
        if(change.getRevisionString() != null)
        {
            result.put("revision", change.getRevisionString());
        }
        if(change.getAction() != null)
        {
            result.put("action", change.getAction().toString().toLowerCase());
        }

        return result;
    }

    public Vector<Hashtable<String, Object>> getArtifactsInBuild(String token, final String projectName, final int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            final Project project = internalGetProject(projectName, true);
            final Vector<Hashtable<String, Object>> result = new Vector<Hashtable<String, Object>>();

            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    final BuildResult build = internalGetBuild(project, id);

                    build.getRoot().forEachNode(new UnaryFunction<RecipeResultNode>()
                    {
                        public void process(RecipeResultNode recipeResultNode)
                        {
                            RecipeResult recipeResult = recipeResultNode.getResult();
                            if(recipeResult != null)
                            {
                                String stage = recipeResultNode.getStageName();
                                for(CommandResult commandResult: recipeResult.getCommandResults())
                                {
                                    String command = commandResult.getCommandName();
                                    for(StoredArtifact artifact: commandResult.getArtifacts())
                                    {
                                        result.add(convertArtifact(artifact, projectName, build, stage, command));
                                    }
                                }
                            }
                        }
                    });
                }
            });

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    private Hashtable<String, Object> convertArtifact(StoredArtifact artifact, String project, BuildResult build, String stage, String command)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put("id", Long.toString(artifact.getId()));
        result.put("stage", stage);
        result.put("command", command);
        result.put("name", artifact.getName());
        result.put("permalink", StringUtils.join("/", "display/projects",
                                                 StringUtils.uriComponentEncode(project),
                                                 "builds", Long.toString(build.getNumber()),
                                                 StringUtils.uriComponentEncode(stage),
                                                 StringUtils.uriComponentEncode(command),
                                                 StringUtils.uriComponentEncode(artifact.getName())));
        return result;
    }

    public Vector<Hashtable<String, String>> getMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            final Project project = internalGetProject(projectName, true);
            final Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();

            buildManager.executeInTransaction(new Runnable()
            {
                public void run()
                {
                    final BuildResult build = internalGetBuild(project, id);
                    build.loadFeatures(configurationManager.getDataDirectory());
                    for(Feature f: build.getFeatures())
                    {
                        result.add(convertFeature(null, null, null, null, f));
                    }

                    build.getRoot().forEachNode(new UnaryFunction<RecipeResultNode>()
                    {
                        public void process(RecipeResultNode recipeResultNode)
                        {
                            RecipeResult recipeResult = recipeResultNode.getResult();
                            if(recipeResult != null)
                            {
                                String stage = recipeResultNode.getStageName();
                                for(Feature f: recipeResult.getFeatures())
                                {
                                    result.add(convertFeature(stage, null, null, null, f));
                                }

                                for(CommandResult commandResult: recipeResult.getCommandResults())
                                {
                                    String command = commandResult.getCommandName();
                                    for(Feature f: commandResult.getFeatures())
                                    {
                                        result.add(convertFeature(stage, command, null, null, f));
                                    }

                                    for(StoredArtifact artifact: commandResult.getArtifacts())
                                    {
                                        String artifactName = artifact.getName();
                                        for(StoredFileArtifact fileArtifact: artifact.getChildren())
                                        {
                                            String artifactPath = fileArtifact.getPath();
                                            for(Feature f: fileArtifact.getFeatures())
                                            {
                                                result.add(convertFeature(stage, command, artifactName, artifactPath, f));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
            });

            return result;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Vector<Hashtable<String, String>> getErrorMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.ERROR);
    }

    public Vector<Hashtable<String, String>> getWarningMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.WARNING);
    }

    public Vector<Hashtable<String, String>> getInfoMessagesInBuild(String token, String projectName, final int id) throws AuthenticationException
    {
        return getMessagesOfLevel(token, projectName, id, Feature.Level.INFO);
    }

    private Vector<Hashtable<String, String>> getMessagesOfLevel(String token, String projectName, int id, Feature.Level level) throws AuthenticationException
    {
        Vector<Hashtable<String, String>> result = getMessagesInBuild(token, projectName, id);
        Iterator<Hashtable<String, String>> it = result.iterator();
        String levelString = level.getPrettyString();
        while(it.hasNext())
        {
            Hashtable<String, String> feature = it.next();
            if(!levelString.equals(feature.get("level")))
            {
                it.remove();
            }
        }

        return result;
    }

    private Hashtable<String, String> convertFeature(String stageName, String commandName, String artifactName, String artifactPath, Feature feature)
    {
        Hashtable<String, String> result = new Hashtable<String, String>();
        if(stageName != null)
        {
            result.put("stage", stageName);
        }

        if(commandName != null)
        {
            result.put("command", commandName);
        }

        if(artifactName != null)
        {
            result.put("artifact", artifactName);
        }

        if(artifactPath != null)
        {
            result.put("path", artifactPath);
        }

        result.put("level", feature.getLevel().getPrettyString());
        result.put("message", feature.getSummary());
        return result;
    }

    public boolean triggerBuild(String token, String projectName) throws AuthenticationException
    {
        return triggerBuild(token, projectName, null);
    }

    public boolean triggerBuild(String token, String projectName, String revision) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, false);

            Revision r = null;
            if(TextUtils.stringSet(revision))
            {
                ScmClient client = null;
                try
                {
                    client = scmClientFactory.createClient(project.getConfig().getScm());
                    r = client.getRevision(revision);
                }
                catch (ScmException e)
                {
                    throw new IllegalArgumentException("Unable to verify revision: " + e.getMessage());
                }
                finally
                {
                    ScmClientUtils.close(client);
                }
            }

            projectManager.triggerBuild(project.getConfig(), new RemoteTriggerBuildReason(), r, true);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public String getProjectState(String token, String projectName) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Project project = internalGetProject(projectName, true);
            return project.getState().toString().toLowerCase();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public Hashtable<String, String> preparePersonalBuild(String token, String projectName, String buildSpecification) throws AuthenticationException, ScmException
    {
        User user = tokenManager.loginUser(token);
        try
        {
            if(!userManager.getPrinciple(user).hasAuthority(ServerPermission.PERSONAL_BUILD.toString()))
            {
                throw new AccessDeniedException("User does not have authority to submit personal build requests.");
            }

            Project project = internalGetProject(projectName, false);
            Hashtable<String, String> scmDetails = new Hashtable<String, String>();
            ScmConfiguration scm = project.getConfig().getScm();
            scmDetails.put(ScmLocation.TYPE, scm.getType());
            ScmClient client = scmClientFactory.createClient(scm);
            scmDetails.put(ScmLocation.LOCATION, client.getLocation());
            return scmDetails;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean pauseProject(String token, String projectName) throws AuthenticationException
    {
        try
        {
            tokenManager.loginUser(token);
            Project project = internalGetProject(projectName, true);
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
            Project project = internalGetProject(projectName, true);
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

    public String getAgentStatus(String token, String name) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                return "";
            }

            return agent.getStatus().getPrettyString();
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean enableAgent(String token, String name) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                throw new IllegalArgumentException("Unknown agent '" + name + "'");
            }

            agentManager.setAgentState(agent.getConfig(), AgentState.EnableState.ENABLED);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean disableAgent(String token, String name) throws AuthenticationException
    {
        tokenManager.loginUser(token);
        try
        {
            Agent agent = agentManager.getAgent(name);
            if (agent == null)
            {
                throw new IllegalArgumentException("Unknown agent '" + name + "'");
            }

            agentManager.setAgentState(agent.getConfig(), AgentState.EnableState.DISABLED);
            return true;
        }
        finally
        {
            tokenManager.logoutUser();
        }
    }

    public boolean shutdown(String token, boolean force, boolean exitJvm) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);

        // Sigh ... this is tricky, because if we shutdown here Jetty dies
        // before this request is complete and the client gets an error :-|.
        shutdownManager.delayedShutdown(force, exitJvm);
        return true;
    }

    public boolean stopService(String token) throws AuthenticationException
    {
        tokenManager.verifyAdmin(token);
        shutdownManager.delayedStop();
        return true;
    }

    private Project internalGetProject(String projectName, boolean allownIvalid)
    {
        Project project = projectManager.getProject(projectName, allownIvalid);
        if (project == null)
        {
            throw new IllegalArgumentException("Unknown project '" + projectName + "'");
        }
        return project;
    }

    private BuildResult internalGetBuild(Project project, int id)
    {
        BuildResult build = buildManager.getByProjectAndNumber(project, id);
        if (build == null)
        {
            throw new IllegalArgumentException("Unknown build '" + id + "' for project '" + project.getName() + "'");
        }

        return build;
    }

    public void handleEvent(Event evt)
    {
        // Rewire on startup to get the full token manager.
        ComponentContext.autowire(this);
        eventManager.unregister(this);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{SystemStartedEvent.class};
    }

    public void setTokenManager(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setScmClientFactory(ScmClientFactory<ScmConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
