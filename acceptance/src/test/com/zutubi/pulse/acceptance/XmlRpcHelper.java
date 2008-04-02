package com.zutubi.pulse.acceptance;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationRegistry;
import static com.zutubi.prototype.type.record.PathUtils.getPath;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.project.BuildStageConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.prototype.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

/**
 */
public class XmlRpcHelper
{
    public static final String SYMBOLIC_NAME_KEY = "meta.symbolicName";
    
    protected XmlRpcClient xmlRpcClient;
    protected String token = null;

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

    public <T> T getConfig(String path) throws Exception
    {
        return (T)call("getConfig", path);
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

    public Vector<String> getAllUserLogins() throws Exception
    {
        return call("getAllUserLogins");
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

    public Vector<String> getAllAgentNames() throws Exception
    {
        return call("getAllAgentNames");
    }

    public String insertSimpleProject(String name, boolean template) throws Exception
    {
        return insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, template);
    }

    public String insertSimpleProject(String name, String parent, boolean template) throws Exception
    {
        Hashtable<String, Object> scm = createEmptyConfig("zutubi.subversionConfig");
        scm.put("url", "svn://localhost:3088/accept/trunk/triviant");
        scm.put("checkoutScheme", "CLEAN_CHECKOUT");
        scm.put("monitor", false);

        Hashtable<String, Object> type = createEmptyConfig("zutubi.antTypeConfig");
        type.put("file", "build.xml");

        Hashtable<String, Object> stage = createEmptyConfig(BuildStageConfiguration.class);
        stage.put("name", "default");
        Hashtable<String, Object> stages = new Hashtable<String, Object>();
        stages.put("default", stage);

        Hashtable<String, Object> project = createEmptyConfig("zutubi.projectConfig");
        project.put("name", name);
        project.put("scm", scm);
        project.put("type", type);
        project.put("stages", stages);

        return call("insertTemplatedConfig", "projects/" + parent, project, template);
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
        String propertiesPath = getPath(ConfigurationRegistry.PROJECTS_SCOPE, project, "properties");
        Hashtable<String, Object> property = createProperty(name, value, resolveVariables, addToEnvironment, addToPath);
        return insertConfig(propertiesPath, property);
    }

    public Hashtable<String, Object> createProperty(String name, String value)
    {
        return createProperty(name, value, false, false, false);
    }

    public Hashtable<String, Object> createProperty(String name, String value, boolean resolveVariables, boolean addToEnvironment, boolean addToPath)
    {
        Hashtable<String, Object> property = createEmptyConfig(ResourceProperty.class);
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

    public String insertTrivialUser(String login) throws Exception
    {
        Hashtable<String, Object> user = createDefaultConfig(UserConfiguration.class);
        user.put("login", login);
        user.put("name", login);
        String path = insertConfig(ConfigurationRegistry.USERS_SCOPE, user);
        Hashtable <String, Object> password = createEmptyConfig(SetPasswordConfiguration.class);
        password.put("password", "");
        password.put("confirmPassword", "");
        doConfigActionWithArgument(path, "setPassword", password);
        return path;
    }

    public String insertGroup(String name, List<String> memberPaths, String... serverPermissions) throws Exception
    {
        Hashtable<String, Object> group = createDefaultConfig(GroupConfiguration.class);
        group.put("name", name);
        group.put("members", new Vector<String>(memberPaths));
        group.put("serverPermissions", new Vector<String>(Arrays.asList(serverPermissions)));
        return insertConfig(ConfigurationRegistry.GROUPS_SCOPE, group);
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
    
    public void triggerBuild(String projectName) throws Exception
    {
        call("triggerBuild", projectName);
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

    public int runBuild(String projectName, long timeout) throws Exception
    {
        int number = getNextBuildNumber(projectName);
        triggerBuild(projectName);

        long startTime = System.currentTimeMillis();
        while(true)
        {
            if(System.currentTimeMillis() - startTime > timeout)
            {
                throw new TimeoutException("Timed out waiting for build " + number + " of project '" + projectName + "' to complete");
            }

            Thread.sleep(500);
            Hashtable<String, Object> build = getBuild(projectName, number);
            if(build != null && Boolean.TRUE.equals(build.get("completed")))
            {
                return number;
            }
        }
    }
}
