package com.zutubi.pulse.acceptance;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectAclConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import org.apache.xmlrpc.XmlRpcClient;

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

    public <T> T getConfig(String path) throws Exception
    {
        return (T)call("getConfig", path);
    }

    public String getConfigHandle(String path) throws Exception
    {
        return call("getConfigHandle", path);
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

    public String insertSimpleProject(String name, boolean template) throws Exception
    {
        return insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, template);
    }

    public String insertSimpleProject(String name, String parent, boolean template) throws Exception
    {
        Hashtable<String, Object> scm = createEmptyConfig("zutubi.svnConfig");
        scm.put("url", "svn://localhost:3088/accept/trunk/triviant");
        scm.put("checkoutScheme", "CLEAN_CHECKOUT");
        scm.put("monitor", false);

        Hashtable<String, Object> type = createEmptyConfig("zutubi.antTypeConfig");
        type.put("file", "build.xml");

        Hashtable<String, Object> project = createEmptyConfig("zutubi.projectConfig");
        project.put("name", name);
        project.put("scm", scm);
        project.put("type", type);

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

    public String addProjectPermissions(String projectPath, String groupPath, String... actions) throws Exception
    {
        Hashtable<String, Object> permission = createDefaultConfig(ProjectAclConfiguration.class);
        permission.put("group", groupPath);
        permission.put("allowedActions", new Vector<String>(Arrays.asList(actions)));
        return insertConfig(PathUtils.getPath(projectPath, "permissions"), permission);
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
        return insertConfig(ConfigurationRegistry.USERS_SCOPE, user);
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
}
