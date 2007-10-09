package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.model.ProjectManager;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
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

    public boolean configPathExists(String path) throws Exception
    {
        return (Boolean)call("configPathExists", path);
    }

    public <T> T getConfig(String path) throws Exception
    {
        return (T)call("getConfig", path);
    }

    public String getConfigHandle(String path) throws Exception
    {
        return call("getConfigHandle", path);
    }

    public String insertConfig(String path, Hashtable<String, Object> config) throws Exception
    {
        return call("insertConfig", path, config);
    }

    public String saveConfig(String path, Hashtable<String, Object> config, boolean deep) throws Exception
    {
        return call("saveConfig", path, config, deep);
    }

    public String insertSimpleProject(String name, boolean template) throws Exception
    {
        return insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, template);
    }

    public String insertSimpleProject(String name, String parent, boolean template) throws Exception
    {
        Hashtable<String, Object> scm = new Hashtable<String, Object>();
        scm.put(BaseXmlRpcAcceptanceTest.SYMBOLIC_NAME_KEY, "zutubi.svnConfig");
        scm.put("url", "svn://localhost:3088/accept/trunk/triviant");
        scm.put("checkoutScheme", "CLEAN_CHECKOUT");
        scm.put("monitor", false);

        Hashtable<String, Object> type = new Hashtable<String, Object>();
        type.put(BaseXmlRpcAcceptanceTest.SYMBOLIC_NAME_KEY, "zutubi.antTypeConfig");
        type.put("file", "build.xml");

        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(BaseXmlRpcAcceptanceTest.SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
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
        Hashtable<String, Object> project = new Hashtable<String, Object>();
        project.put(BaseXmlRpcAcceptanceTest.SYMBOLIC_NAME_KEY, "zutubi.projectConfig");
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

    public String insertSimpleAgent(String name) throws Exception
    {
        Hashtable<String, Object> agent = new Hashtable<String, Object>();
        agent.put(SYMBOLIC_NAME_KEY, "zutubi.agentConfig");
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
}
