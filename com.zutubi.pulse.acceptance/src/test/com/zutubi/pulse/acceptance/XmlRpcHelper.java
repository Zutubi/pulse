package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.PostStageHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.Condition;
import com.zutubi.util.Pair;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 */
public class XmlRpcHelper
{
    public static final String SYMBOLIC_NAME_KEY = "meta.symbolicName";

    private static final int INTIALISATION_TIMEOUT = 90000;

    protected XmlRpcClient xmlRpcClient;
    protected String token = null;

    private static final long BUILD_TIMEOUT = 90000;

    public XmlRpcHelper() throws MalformedURLException
    {
        this(new URL("http", "localhost", AcceptanceTestUtils.getPulsePort(), "/xmlrpc"));
    }

    public XmlRpcHelper(String url) throws MalformedURLException
    {
        this(new URL(url));
    }

    public XmlRpcHelper(URL url)
    {
        xmlRpcClient = new XmlRpcClient(url);
    }

    public String login(String login, String password) throws Exception
    {
        token = (String) callWithoutToken("login", login, password);
        return token;
    }

    public String loginAsAdmin() throws Exception
    {
        return login("admin", "admin");
    }

    public boolean logout() throws Exception
    {
        verifyLoggedIn();
        Object result = callWithoutToken("logout", token);
        token = null;
        return (Boolean)result;
    }

    public boolean isLoggedIn()
    {
        return token != null;
    }

    private void verifyLoggedIn()
    {
        if(!isLoggedIn())
        {
            throw new IllegalStateException("Not logged in, call login first");
        }
    }

    public Vector<Object> getVector(Object... o)
    {
        return new Vector<Object>(Arrays.asList(o));
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T callWithoutToken(String function, Object... args) throws Exception
    {
        return (T) xmlRpcClient.execute("RemoteApi." + function, getVector(args));
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T call(String function, Object... args) throws Exception
    {
        verifyLoggedIn();
        Vector<Object> argVector = new Vector<Object>(args.length + 1);
        argVector.add(token);
        argVector.addAll(Arrays.asList(args));
        return (T) xmlRpcClient.execute("RemoteApi." + function, argVector);
    }

    public Hashtable<String, String> getServerInfo() throws Exception
    {
        return call("getServerInfo");
    }

    public String getSymbolicName(Class<? extends Configuration> clazz)
    {
        return clazz.getAnnotation(SymbolicName.class).value();
    }

    public boolean configPathExists(String path) throws Exception
    {
        return (Boolean)call("configPathExists", path);
    }

    public Hashtable<String, Object> createEmptyConfig(Class<? extends Configuration> clazz)
    {
        return createEmptyConfig(getSymbolicName(clazz));
    }

    public Hashtable<String, Object> createEmptyConfig(String symbolicName)
    {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        result.put(SYMBOLIC_NAME_KEY, symbolicName);
        return result;
    }

    public Hashtable<String, Object> createDefaultConfig(Class<? extends Configuration> clazz) throws Exception
    {
        return createDefaultConfig(getSymbolicName(clazz));
    }

    public Hashtable<String, Object> createDefaultConfig(String symbolicName) throws Exception
    {
        return call("createDefaultConfig", symbolicName);
    }

    public Vector<String> getConfigListing(String path) throws Exception
    {
        return call("getConfigListing", path);
    }

    public String getTemplateParent(String path) throws Exception
    {
        return call("getTemplateParent", path);
    }

    public Vector<String> getTemplateChildren(String path) throws Exception
    {
        return call("getTemplateChildren", path);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getConfig(String path) throws Exception
    {
        return (T) call("getConfig", path);
    }

    public String getConfigHandle(String path) throws Exception
    {
        return call("getConfigHandle", path);
    }

    public boolean isConfigPermanent(String path) throws Exception
    {
        return (Boolean) call("isConfigPermanent", path);
    }

    public boolean isConfigValid(String path) throws Exception
    {
        return (Boolean) call("isConfigValid", path);
    }

    public String insertConfig(String path, Hashtable<String, Object> config) throws Exception
    {
        return call("insertConfig", path, config);
    }

    public String insertTemplatedConfig(String path, Hashtable<String, Object> config, boolean template) throws Exception
    {
        return call("insertTemplatedConfig", path, config, template);
    }

    public String saveConfig(String path, Hashtable<String, Object> config, boolean deep) throws Exception
    {
        return call("saveConfig", path, config, deep);
    }

    public boolean deleteConfig(String path) throws Exception
    {
        return (Boolean) call("deleteConfig", path);
    }

    public void deleteAllConfigs(String pathPattern) throws Exception
    {
        call("deleteAllConfigs", pathPattern);
    }

    public void restoreConfig(String path) throws Exception
    {
        call("restoreConfig", path);
    }

    public void setConfigOrder(String path, String... order) throws Exception
    {
        call("setConfigOrder", path, new Vector<String>(Arrays.asList(order)));
    }

    public Vector<String> getConfigActions(String path) throws Exception
    {
        return call("getConfigActions", path);
    }

    public void doConfigAction(String path, String action) throws Exception
    {
        call("doConfigAction", path, action);
    }

    public void doConfigActionWithArgument(String path, String action, Hashtable<String, Object> argument) throws Exception
    {
        call("doConfigActionWithArgument", path, action, argument);
    }

    public int getUserCount() throws Exception
    {
        return (Integer) call("getUserCount");
    }

    public Vector<String> getAllUserLogins() throws Exception
    {
        return call("getAllUserLogins");
    }

    public Hashtable<String, Object> getProject(String name) throws Exception
    {
        return call("getProject", name);
    }
    
    public int getProjectCount() throws Exception
    {
        return (Integer) call("getProjectCount");
    }

    public Vector<String> getAllProjectNames() throws Exception
    {
        return call("getAllProjectNames");
    }

    public Vector<String> getMyProjectNames() throws Exception
    {
        return call("getMyProjectNames");
    }

    public Vector<String> getAllProjectGroups() throws Exception
    {
        return call("getAllProjectGroups");
    }

    public Hashtable<String, Object> getProjectGroup(String name) throws Exception
    {
        return call("getProjectGroup", name);
    }

    /**
     * Retrieves the current state of a project.
     *
     * @param projectName the project to get the state of
     * @return the project's current state
     * @throws Exception on error
     */
    public Project.State getProjectState(String projectName) throws Exception
    {
        String stateString = call("getProjectState", projectName);
        return Project.State.valueOf(stateString.replace(' ', '_').toUpperCase());
    }

    @SuppressWarnings({"unchecked"})
    public Hashtable<String, Object> getProjectCapture(String projectName, String recipeName, String commandName, String captureName) throws Exception
    {
        Hashtable<String, Object> projectConfig = getConfig(MasterConfigurationRegistry.PROJECTS_SCOPE + "/" + projectName);
        Hashtable<String, Object> projectType = (Hashtable<String, Object>) projectConfig.get(Constants.Project.TYPE);
        Hashtable<String, Object> recipes = (Hashtable<String, Object>) projectType.get("recipes");
        Hashtable<String, Object> recipe = (Hashtable<String, Object>) recipes.get(recipeName);
        Hashtable<String, Object> commands = (Hashtable<String, Object>) recipe.get("commands");
        Hashtable<String, Object> command = (Hashtable<String, Object>) commands.get(commandName);
        Hashtable<String, Object> captures = (Hashtable<String, Object>) command.get(Constants.Project.Command.CAPTURES);
        return (Hashtable<String, Object>) captures.get(captureName);
    }

    public int getAgentCount() throws Exception
    {
        return (Integer) call("getAgentCount");
    }

    public Object getAgentStatus(String name) throws Exception
    {
        return call("getAgentStatus", name);
    }

    public Vector<String> getAllAgentNames() throws Exception
    {
        return call("getAllAgentNames");
    }

    public Vector<Hashtable<String, Object>> getArtifactsInBuild(String project, long buildNumber) throws Exception
    {
        return call("getArtifactsInBuild", project, (int)buildNumber);
    }

    public String insertSimpleProject(String name) throws Exception
    {
        return insertSimpleProject(name, false);
    }

    public String insertSimpleProject(String name, boolean template) throws Exception
    {
        return insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, template);
    }

    public String insertSimpleProject(String name, String parent, boolean template) throws Exception
    {
        return insertSingleCommandProject(name, parent, template, getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), getAntConfig());
    }

    public String insertSingleCommandProject(String name, String parent, boolean template, Hashtable<String, Object> scm, Hashtable<String, Object> command) throws Exception
    {
        Hashtable<String, Object> commands = new Hashtable<String, Object>();
        String commandName = (String) command.get("name");
        if (commandName == null)
        {
            commandName = "build";
            command.put("name", commandName);
        }

        commands.put(commandName, command);

        Hashtable<String, Object> recipe = createEmptyConfig(RecipeConfiguration.class);
        recipe.put("name", "default");
        recipe.put("commands", commands);

        Hashtable<String, Object> recipes = new Hashtable<String, Object>();
        recipes.put("default", recipe);

        Hashtable<String, Object> type = createEmptyConfig(MultiRecipeTypeConfiguration.class);
        type.put("defaultRecipe", "default");
        type.put("recipes", recipes);

        return insertProject(name, parent, template, scm, type);
    }

    public String insertProject(String name, String parent, boolean template, Hashtable<String, Object> scm, Hashtable<String, Object> type) throws Exception
    {
        Hashtable<String, Object> stage = createEmptyConfig(BuildStageConfiguration.class);
        stage.put("name", "default");
        Hashtable<String, Object> stages = new Hashtable<String, Object>();
        stages.put("default", stage);

        Hashtable<String, Object> project = createEmptyConfig(ProjectConfiguration.class);
        project.put("name", name);
        if (scm != null)
        {
            project.put("scm", scm);
        }
        if (type != null)
        {
            project.put("type", type);
        }
        project.put("stages", stages);

        String path = call("insertTemplatedConfig", "projects/" + parent, project, template);
        if (!template)
        {
            waitForProjectToInitialise(name);
        }

        return path;
    }

    public Hashtable<String, Object> getSubversionConfig(String url)
    {
        Hashtable<String, Object> scm = createEmptyConfig(SubversionConfiguration.class);
        scm.put("url", url);
        scm.put("checkoutScheme", "CLEAN_CHECKOUT");
        scm.put("monitor", false);
        return scm;
    }

    public Hashtable<String, Object> getGitConfig(String url)
    {
        Hashtable<String, Object> gitConfig = createEmptyConfig("zutubi.gitConfig");
        gitConfig.put("repository", url);
        gitConfig.put("checkoutScheme", "CLEAN_CHECKOUT");
        gitConfig.put("monitor", false);
        return gitConfig;
    }

    public Hashtable<String, Object> getAntConfig()
    {
        Hashtable<String, Object> type = createEmptyConfig(AntCommandConfiguration.class);
        type.put("buildFile", "build.xml");
        return type;
    }

    public Hashtable<String, Object> getCustomTypeConfig(String pulseFileString)
    {
        Hashtable<String, Object> type = createEmptyConfig(CustomTypeConfiguration.class);
        type.put("pulseFileString", pulseFileString);
        return type;
    }

    public Hashtable<String, Object> getMultiRecipeTypeConfig()
    {
        return createEmptyConfig(MultiRecipeTypeConfiguration.class);
    }

    public Hashtable<String, Object> createVersionedConfig(String pulseFilePath) throws Exception
    {
        Hashtable<String, Object> versionedConfig = createDefaultConfig(VersionedTypeConfiguration.class);
        versionedConfig.put("pulseFileName", pulseFilePath);
        return versionedConfig;
    }
    
    public void waitForProjectToInitialise(String name) throws Exception
    {
        long startTime = System.currentTimeMillis();
        Project.State state;
        while (true)
        {
            state = getProjectState(name);
            if (state.isInitialised())
            {
                break;
            }

            Thread.sleep(50);
            if (System.currentTimeMillis() - startTime > INTIALISATION_TIMEOUT)
            {
                throw new RuntimeException("Timed out waiting for project '" + name + "' to init (state is '" + state + "')");
            }
        }
    }

    public String insertTrivialProject(String name, boolean template) throws Exception
    {
        return insertTrivialProject(name, ProjectManager.GLOBAL_PROJECT_NAME, template);
    }

    public String insertTrivialProject(String name, String parent, boolean template) throws Exception
    {
        Hashtable<String, Object> project = createEmptyConfig("zutubi.projectConfig");
        project.put("name", name);

        return call("insertTemplatedConfig", "projects/" + parent, project, template);
    }

    public boolean ensureProject(String name) throws Exception
    {
        if(!configPathExists("projects/" + name))
        {
            insertSimpleProject(name, false);
            return true;
        }

        return false;
    }

    public String insertProjectProperty(String project, String name, String value) throws Exception
    {
        return insertProjectProperty(project, name, value, false, false, false);
    }

    public String insertProjectProperty(String project, String name, String value, boolean resolveVariables, boolean addToEnvironment, boolean addToPath) throws Exception
    {
        String propertiesPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, "properties");
        Hashtable<String, Object> property = createProperty(name, value, resolveVariables, addToEnvironment, addToPath);
        return insertConfig(propertiesPath, property);
    }

    public String insertOrUpdateStageProperty(String project, String stage, String name, String value) throws Exception
    {
        String propertiesPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, "stages", stage, "properties");
        Hashtable<String, Object> properties = getConfig(propertiesPath);
        if (properties.containsKey(name))
        {
            @SuppressWarnings({"unchecked"})
            Hashtable<String, Object> property = (Hashtable<String, Object>) properties.get(name);
            property.put("value", value);
            return saveConfig(getPath(propertiesPath, name), property, false);
        }
        else
        {
            Hashtable<String, Object> property = createProperty(name, value, false, false, false);
            return insertConfig(propertiesPath, property);
        }
    }

    public Hashtable<String, Object> createProperty(String name, String value)
    {
        return createProperty(name, value, false, false, false);
    }

    public Hashtable<String, Object> createProperty(String name, String value, boolean resolveVariables, boolean addToEnvironment, boolean addToPath)
    {
        Hashtable<String, Object> property = createEmptyConfig(ResourcePropertyConfiguration.class);
        property.put("name", name);
        property.put("value", value);
        property.put("resolveVariables", resolveVariables);
        property.put("addToEnvironment", addToEnvironment);
        property.put("addToPath", addToPath);
        return property;
    }

    public String addProjectPermissions(String projectPath, String groupPath, String... actions) throws Exception
    {
        Hashtable<String, Object> permission = createDefaultConfig(ProjectAclConfiguration.class);
        permission.put("group", groupPath);
        permission.put("allowedActions", new Vector<String>(Arrays.asList(actions)));
        return insertConfig(getPath(projectPath, "permissions"), permission);
    }

    public String insertPostStageHook(String project, String name, String... stageNames) throws Exception
    {
        Hashtable<String, Object> hook = createPostStageHook(project, name, stageNames);
        return insertConfig(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, Constants.Project.HOOKS), hook);
    }

    public Hashtable<String, Object> createPostStageHook(String project, String name, String... stageNames) throws Exception
    {
        Hashtable<String, Object> hook = createDefaultConfig(PostStageHookConfiguration.class);
        hook.put("name", name);
        hook.put("applyToAllStages", stageNames.length == 0);
        if (stageNames.length > 0)
        {
            String stagesPath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, Constants.Project.STAGES);
            Vector<String> stages = new Vector<String>();
            for (String stageName: stageNames)
            {
                stages.add(PathUtils.getPath(stagesPath, stageName));
            }
            hook.put("stages", stages);
        }

        return hook;
    }

    public void enableBuildPrompting(String projectName) throws Exception
    {
        Hashtable<String, Object> config = getConfig(getOptionsPath(projectName));
        config.put("prompt", Boolean.TRUE);
        saveConfig(getOptionsPath(projectName), config, false);
    }

    private String getOptionsPath(String projectName)
    {
        return "projects/" + projectName + "/options";
    }

    public String insertSimpleAgent(String name) throws Exception
    {
        Hashtable<String, Object> agent = createEmptyConfig("zutubi.agentConfig");
        agent.put("name", name);
        agent.put("host", name);
        agent.put("port", 8890);

        return call("insertTemplatedConfig", "agents/global agent template", agent, false);
    }

    public String ensureAgent(String name) throws Exception
    {
        String path = "agents/" + name;
        if(!configPathExists(path))
        {
            insertSimpleAgent(name);
        }

        return path;
    }

    public boolean ensureUser(String login) throws Exception
    {
        if(!configPathExists("users/" + login))
        {
            insertTrivialUser(login);
            return true;
        }

        return false;
    }

    public String insertTrivialUser(String login) throws Exception
    {
        Hashtable<String, Object> user = createDefaultConfig(UserConfiguration.class);
        user.put("login", login);
        user.put("name", login);
        String path = insertConfig(MasterConfigurationRegistry.USERS_SCOPE, user);
        Hashtable <String, Object> password = createEmptyConfig(SetPasswordConfiguration.class);
        password.put("password", "");
        password.put("confirmPassword", "");
        doConfigActionWithArgument(path, "setPassword", password);
        return path;
    }

    public String insertGroup(String name, List<String> memberPaths, String... serverPermissions) throws Exception
    {
        Hashtable<String, Object> group = createDefaultConfig(UserGroupConfiguration.class);
        group.put("name", name);
        group.put("members", new Vector<String>(memberPaths));
        group.put("serverPermissions", new Vector<String>(Arrays.asList(serverPermissions)));
        return insertConfig(MasterConfigurationRegistry.GROUPS_SCOPE, group);
    }

    public void logError(String message) throws Exception
    {
        call("logError", message);
    }

    public void logWarning(String message) throws Exception
    {
        call("logWarning", message);
    }

    public int getNextBuildNumber(String projectName) throws Exception
    {
        return (Integer) call("getNextBuildNumber", projectName);
    }

    public void setNextBuildNumber(String projectName, long number) throws Exception
    {
        call("setNextBuildNumber", projectName, Long.toString(number));
    }

    public void triggerBuild(String projectName) throws Exception
    {
        call("triggerBuild", projectName);
    }

    public void triggerBuild(String projectName, String revision, Hashtable<String, String> properties) throws Exception
    {
        call("triggerBuild", projectName, revision, properties);
    }

    public void triggerBuild(String projectName, String revision, String status, Hashtable<String, String> properties) throws Exception
    {
        call("triggerBuild", projectName, revision, status, properties);
    }

    /**
     * Requests the given build be terminated.
     *
     * @param projectName name of the project that is building
     * @param number      the build number to terminate
     * @return true if the build was found and in progress when the request was
     *         made
     * @throws Exception on error
     */
    public boolean cancelBuild(String projectName, int number) throws Exception
    {
        return (Boolean) call("cancelBuild", projectName, number);
    }

    public Hashtable<String, Object> getBuild(String projectName, int number) throws Exception
    {
        Vector<Hashtable<String, Object>> build = call("getBuild", projectName, number);
        if(build.size() == 0)
        {
            return null;
        }
        else
        {
            return build.get(0);
        }
    }

    public boolean deleteBuild(String projectName, int number) throws Exception
    {
        return (Boolean)call("deleteBuild", projectName, number);
    }

    /**
     * Triggers a build of the given project and waits for the default amount of
     * time for it to complete.
     *
     * @param projectName   name of the project to trigger
     * @param options       the key value pairs describing the build options
     * @return  the build number
     * @throws Exception    on error.
     */
    public int runBuild(String projectName, Pair<String, Object>... options) throws Exception
    {
        Hashtable<String, Object> triggerOptions = new Hashtable<String, Object>();
        for (Pair<String, Object> option: options)
        {
            triggerOptions.put(option.getFirst(), option.getSecond());
        }
        
        int number = getNextBuildNumber(projectName);
        call("triggerBuild", projectName, triggerOptions);
        waitForBuildToComplete(projectName, number);
        return number;
    }

    /**
     * Triggers a build of the given project and waits for up to timeout
     * milliseconds for it to complete.
     *
     * @param projectName name of the project to trigger
     * @param timeout     maximum number of milliseconds to wait for the build
     * @return the build number
     * @throws Exception on any error
     */
    public int runBuild(String projectName, long timeout) throws Exception
    {
        int number = getNextBuildNumber(projectName);
        triggerBuild(projectName);
        waitForBuildToComplete(projectName, number, timeout);
        return number;
    }

    /**
     * Triggers a build of the given project and waits for up to the given timeout (milliseconds)
     * for it to complete.
     *
     * @param projectName   the name of the project to trigger
     * @param status        the status of the build being triggered.
     * @param timeout       maximum number of milliseconds to wait for the build
     * @return  the build number
     * @throws Exception is thrown on any error.
     *
     * @see #runBuild(String, long)
     */
    public int runBuild(String projectName, String status, long timeout) throws Exception
    {
        int number = getNextBuildNumber(projectName);
        triggerBuild(projectName, "", status, new Hashtable<String, String>());
        waitForBuildToComplete(projectName, number, timeout);
        return number;
    }

    /**
     * Waits for a project build to be in progress.  Should not be used if
     * there is a risk the build has already completed.
     *
     * @param projectName the project that is building
     * @param number      the build number
     * @param timeout     timeout in milliseconds
     * @throws Exception on error
     */
    public void waitForBuildInProgress(final String projectName, final int number, long timeout) throws Exception
    {
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    Hashtable<String, Object> build = getBuild(projectName, number);
                    return build != null && build.get("status").equals(ResultState.IN_PROGRESS.getPrettyString());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, timeout, "build " + number + " of project " + projectName + " to become in progress");
    }

    public void waitForBuildToComplete(final String projectName, final int number) throws Exception
    {
        waitForBuildToComplete(projectName, number, BUILD_TIMEOUT);    
    }

    /**
     * Waits for a project build to finish.
     *
     * @param projectName the project that is building
     * @param number      the build number
     * @param timeout     timeout in milliseconds
     * @throws Exception on error
     */
    public void waitForBuildToComplete(final String projectName, final int number, long timeout) throws Exception
    {
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    Hashtable<String, Object> build = getBuild(projectName, number);
                    return build != null && Boolean.TRUE.equals(build.get("completed"));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, timeout, "build " + number + " of project " + projectName + " to complete");
    }

    public Hashtable<String, String> getResponsibilityInfo(String projectName) throws Exception
    {
        return call("getResponsibilityInfo", projectName);
    }

    public void takeResponsibility(String projectName, String comment) throws Exception
    {
        call("takeResponsibility", projectName, comment);
    }

    public boolean clearResponsibility(String projectName) throws Exception
    {
        return (Boolean) call("clearResponsibility", projectName);
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsForProject(String project, boolean completedOnly, int maxResults) throws Exception
    {
        return call("getLatestBuildsForProject", project, completedOnly, maxResults);
    }

    public Vector<Hashtable<String, Object>> getLatestBuildsWithWarnings(String project, int maxResults) throws Exception
    {
        return call("getLatestBuildsWithWarnings", project, maxResults);
    }

    public Vector<Hashtable<String, Object>> queryBuildsForProject(String project, Vector<String> resultStates, int firstResult, int maxResults, boolean mostRecentFirst) throws Exception
    {
        return call("queryBuildsForProject", project, resultStates, firstResult, maxResults, mostRecentFirst);
    }

    public Vector<Hashtable<String, Object>> getBuildQueueSnapshot() throws Exception
    {
        return call("getBuildQueueSnapshot");
    }

    public boolean cancelQueuedBuildRequest(String id) throws Exception
    {
        return (Boolean) call("cancelQueuedBuildRequest", id);
    }

    public Hashtable<String, Object> getReportData(String projectName, String reportGroup, String report, int timeFrame, String timeUnit) throws Exception
    {
        return call("getReportData", projectName, reportGroup, report, timeFrame, timeUnit);
    }

    public static void main(String[] argv) throws Exception
    {
        XmlRpcHelper helper = new XmlRpcHelper("http://localhost:8080/xmlrpc");
        helper.loginAsAdmin();
        try
        {
            helper.insertSimpleProject(argv[0], false);
        }
        finally
        {
            helper.logout();
        }
    }
}
