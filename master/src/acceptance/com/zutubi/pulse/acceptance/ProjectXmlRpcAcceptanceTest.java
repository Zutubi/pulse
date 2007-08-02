package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.util.RandomUtils;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <class comment/>
 */
public class ProjectXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private String adminToken;
    private static final String PROJECT_DESCRIPTION = "project description";
    private static final String PROJECT_URL = "project url";

    public ProjectXmlRpcAcceptanceTest()
    {
    }

    public ProjectXmlRpcAcceptanceTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        adminToken = (String) xmlRpcClient.execute("RemoteApi.login", getVector("admin", "admin"));
    }

    protected void tearDown() throws Exception
    {
        adminToken = null;

        super.tearDown();
    }

    public void testCreateProject() throws IOException, XmlRpcException
    {
        createProject();
    }

    public void testCreateExeProject() throws IOException, XmlRpcException
    {
        createProject("executable", "executable", "scons.bat", "arguments", "arg1 arg2", "workingDir", "workit");
    }

    public void testDeleteProject() throws IOException, XmlRpcException
    {
        Object result = xmlRpcClient.execute("RemoteApi.getAllProjectNames", getVector(adminToken));
        assertNotNull(result);

        String projectName;
        Vector<String> projectNames = (Vector<String>) result;
        if (projectNames.size() == 0)
        {
            projectName = createProject();
        }
        else
        {
            projectName = projectNames.get(0);
        }

        result = xmlRpcClient.execute("RemoteApi.deleteProject", getVector(adminToken, projectName));
        assertEquals(Boolean.TRUE, result);
    }

    public void testCloneProject() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String name = createProject();

        String newName = String.format("project-%s", RandomUtils.randomString(4));
        String newDescription = "random desc";

        Object result = xmlRpcClient.execute("RemoteApi.cloneProject", getVector(adminToken, name, newName, newDescription));
        assertProject(result, newName, newDescription);
    }

    public void testEditProject() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String name = createProject();

        String newName = String.format("project-%s", RandomUtils.randomString(4));

        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", newName);

        Object result = xmlRpcClient.execute("RemoteApi.editProject", getVector(adminToken, name, projectDetails));
        assertEquals(Boolean.TRUE, result);
    }

    public void testGetProject() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String projectName = createProject();

        Object result = xmlRpcClient.execute("RemoteApi.getProject", getVector(adminToken, projectName));
        assertProject(result, projectName, PROJECT_DESCRIPTION);
    }

    private void assertProject(Object result, String projectName, String projectDescription)
    {
        assertNotNull(result);
        assertTrue(result instanceof Hashtable);
        Hashtable<String, Object> details = (Hashtable<String, Object>) result;

        // check that the necessary entries exist.
        assertEquals(6, details.size());
        assertNotNull(details.get("id"));
        assertEquals(projectName, details.get("name"));
        assertEquals(projectDescription, details.get("description"));
        assertEquals(PROJECT_URL, details.get("url"));
        assertEquals("cvs", details.get("scm"));
        assertEquals("ant", details.get("type"));
    }

    public void testGetProjectBuildSpecifications() throws IOException, XmlRpcException
    {
        String projectName = createProject();

        Object result = xmlRpcClient.execute("RemoteApi.getProjectBuildSpecifications", getVector(adminToken, projectName));
        assertNotNull(result);
        assertTrue(result instanceof Vector);

        Vector<String> names = (Vector<String>) result;
        assertEquals(1, names.size());
        assertEquals("default", names.get(0));
    }

    public void testGetScm() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String projectName = createProject();

        Object result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));
        assertNotNull(result);
        assertTrue(result instanceof Hashtable);

        Hashtable<String, Object> details = (Hashtable<String, Object>) result;
        assertEquals(4, details.size());

        assertEquals(":pserver:someone@localhost:/cvsroot", details.get("root"));
        assertEquals("project", details.get("module"));
        assertEquals("false", details.get("monitor"));
        assertEquals("0", details.get("quietPeriod"));
    }

    public void testEditScm() throws IOException, XmlRpcException
    {
        // create a project with a specific name.
        String projectName = createProject();

        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
        scmDetails.put("root", ":pserver:me@localhost:/cvsroot");
        scmDetails.put("module", "myProject");
        scmDetails.put("monitor", "true");
        scmDetails.put("quietPeriod", "55555");

        Object result = xmlRpcClient.execute("RemoteApi.editScm", getVector(adminToken, projectName, scmDetails));
        assertEquals(Boolean.TRUE, result);

        result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));

        Hashtable<String, Object> details = (Hashtable<String, Object>) result;
        assertEquals(4, details.size());

        assertEquals(":pserver:me@localhost:/cvsroot", details.get("root"));
        assertEquals("myProject", details.get("module"));
        assertEquals("true", details.get("monitor"));
        assertEquals("55555", details.get("quietPeriod"));

        // return the monitor to false.
        scmDetails.put("monitor", "false");

        result = xmlRpcClient.execute("RemoteApi.editScm", getVector(adminToken, projectName, scmDetails));
        assertEquals(Boolean.TRUE, result);

        result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));
        details = (Hashtable<String, Object>) result;
        assertEquals("false", details.get("monitor"));
        
    }

    private String createProject() throws IOException, XmlRpcException
    {
        return createProject("ant", "buildFile", "build.xml");
    }

    private String createProject(String type, String... typeArgs) throws IOException, XmlRpcException
    {
        String project = String.format("project-%s", RandomUtils.randomString(4));

        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
        projectDetails.put("name", project);
        projectDetails.put("description", PROJECT_DESCRIPTION);
        projectDetails.put("url", PROJECT_URL);

        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
        scmDetails.put("type", "cvs");
        scmDetails.put("root", ":pserver:someone@localhost:/cvsroot");
        scmDetails.put("module", "project");

        Hashtable<String, Object> typeDetails = new Hashtable<String, Object>();
        typeDetails.put("type", type);
        for(int i = 0; i < typeArgs.length; i += 2)
        {
            typeDetails.put(typeArgs[i], typeArgs[i + 1]);
        }

        Object result = xmlRpcClient.execute("RemoteApi.createProject", getVector(adminToken, projectDetails, scmDetails, typeDetails));
        assertEquals(Boolean.TRUE, result);
        
        return project;
    }
}
