package com.zutubi.pulse.acceptance;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests configuration of projects via the remote API.
 */
public class ProjectXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
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
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();
        super.tearDown();
    }

    public void testGetGlobal() throws Exception
    {
        Hashtable<String, Object> globalProject = call("getConfig", "projects/global project template");
        assertProject(globalProject, "global project template");
        assertDefaultStages(globalProject);
        assertDefaultOptions(globalProject);
    }

    public void testInsertProject() throws Exception
    {
        String projectName = randomName();

        String path = insertSimpleProject(projectName);
        assertEquals("projects/" + projectName, path);

        Hashtable<String, Object> createdProject = call("getConfig", path);
        assertProject(createdProject, projectName);
        assertDefaultStages(createdProject);
        assertDefaultOptions(createdProject);

        Hashtable<String, Object> createdScm = (Hashtable<String, Object>) createdProject.get("scm");
        assertNotNull(createdScm);
        assertEquals("zutubi.subversionConfig", createdScm.get(SYMBOLIC_NAME_KEY));
        assertEquals("svn://localhost:3088/accept/trunk/triviant", createdScm.get("url"));
        assertEquals("CLEAN_CHECKOUT", createdScm.get("checkoutScheme"));
        assertEquals(false, createdScm.get("monitor"));

        Hashtable<String, Object> createdType = (Hashtable<String, Object>) createdProject.get("type");
        assertNotNull(createdType);
        assertEquals("zutubi.antTypeConfig", createdType.get(SYMBOLIC_NAME_KEY));
        assertEquals("build.xml", createdType.get("file"));
    }

    public void testDeleteProject() throws Exception
    {
        String projectName = randomName();
        String path = insertSimpleProject(projectName);
        Hashtable<String, Object> createdProject = call("getConfig", path);
        assertProject(createdProject, projectName);

        Boolean result = call("deleteConfig", path);
        assertTrue(result);
        assertFalse((Boolean) call("configPathExists", path));
    }

    public void testEditProject() throws Exception
    {
        String projectName = randomName();
        String path = insertSimpleProject(projectName);

        String editedName = projectName + " edited";
        String editedPath = "projects/" + editedName;
        String editedUrl = "svn://localhost/test/edited";

        Hashtable<String, Object> createdProject = call("getConfig", path);
        createdProject.put("name", editedName);
        Hashtable<String, Object> createdScm = (Hashtable<String, Object>) createdProject.get("scm");
        createdScm.put("url", editedUrl);

        call("saveConfig", path, createdProject, true);
        assertFalse((Boolean) call("configPathExists", path));

        Hashtable<String, Object> editedProject = call("getConfig", editedPath);
        assertProject(editedProject, editedName);
        assertDefaultStages(editedProject);
        assertDefaultOptions(editedProject);

        Hashtable<String, Object> editedScm = (Hashtable<String, Object>) editedProject.get("scm");
        assertEquals(editedUrl, editedScm.get("url"));
    }

    public void testListProjects() throws Exception
    {
        Vector<String> projects = call("getConfigListing", "projects");
        assertTrue(projects.contains("global project template"));
        int sizeBefore = projects.size();

        String name = randomName();
        insertSimpleProject(name);

        projects = call("getConfigListing", "projects");
        assertEquals(sizeBefore + 1, projects.size());
        assertTrue(projects.contains(name));
    }

    private void assertProject(Hashtable<String, Object> struct, String name)
    {
        assertEquals("zutubi.projectConfig", struct.get(SYMBOLIC_NAME_KEY));
        assertEquals(name, struct.get("name"));
    }

    private void assertDefaultStages(Hashtable<String, Object> project)
    {
        Hashtable<String, Object> stages = (Hashtable<String, Object>) project.get("stages");
        assertNotNull(stages);
        assertDefaultStage(stages);
    }

    private void assertDefaultStage(Hashtable<String, Object> stages)
    {
        Hashtable<String, Object> defaultStage = (Hashtable<String, Object>) stages.get("default");
        assertNotNull(defaultStage);
        assertEquals("zutubi.stageConfig", defaultStage.get(SYMBOLIC_NAME_KEY));
        assertEquals("default", defaultStage.get("name"));
        assertEquals(0, ((Hashtable)defaultStage.get("properties")).size());
        assertEquals(0, ((Vector)defaultStage.get("requirements")).size());
    }

    private void assertDefaultOptions(Hashtable<String, Object> project)
    {
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertNotNull(options);
        assertEquals("zutubi.buildOptionsConfig", options.get(SYMBOLIC_NAME_KEY));
        assertEquals(false, options.get("isolateChangelists"));
        assertEquals(false, options.get("prompt"));
        assertEquals(false, options.get("retainWorkingCopy"));
        assertEquals(0, options.get("timeout"));
    }

//    public void testCreateProject() throws IOException, XmlRpcException
//    {
//        createProject();
//    }
//
//    public void testCreateExeProject() throws IOException, XmlRpcException
//    {
//        createProject("executable", "executable", "scons.bat", "arguments", "arg1 arg2", "workingDir", "workit");
//    }
//
//    public void testDeleteProject() throws IOException, XmlRpcException
//    {
//        Object result = xmlRpcClient.execute("RemoteApi.getAllProjectNames", getVector(adminToken));
//        assertNotNull(result);
//
//        String projectName;
//        Vector<String> projectNames = (Vector<String>) result;
//        if (projectNames.size() == 0)
//        {
//            projectName = createProject();
//        }
//        else
//        {
//            projectName = projectNames.get(0);
//        }
//
//        result = xmlRpcClient.execute("RemoteApi.deleteProject", getVector(adminToken, projectName));
//        assertEquals(Boolean.TRUE, result);
//    }
//
//    public void testCloneProject() throws IOException, XmlRpcException
//    {
//        // create a project with a specific name.
//        String name = createProject();
//
//        String newName = String.format("project-%s", RandomUtils.randomString(4));
//        String newDescription = "random desc";
//
//        Object result = xmlRpcClient.execute("RemoteApi.cloneProject", getVector(adminToken, name, newName, newDescription));
//        assertProject(result, newName, newDescription);
//    }
//
//    public void testEditProject() throws IOException, XmlRpcException
//    {
//        // create a project with a specific name.
//        String name = createProject();
//
//        String newName = String.format("project-%s", RandomUtils.randomString(4));
//
//        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
//        projectDetails.put("name", newName);
//
//        Object result = xmlRpcClient.execute("RemoteApi.editProject", getVector(adminToken, name, projectDetails));
//        assertEquals(Boolean.TRUE, result);
//    }
//
//    public void testGetProject() throws IOException, XmlRpcException
//    {
//        // create a project with a specific name.
//        String projectName = createProject();
//
//        Object result = xmlRpcClient.execute("RemoteApi.getProject", getVector(adminToken, projectName));
//        assertProject(result, projectName, PROJECT_DESCRIPTION);
//    }
//
//    private void assertProject(Object result, String projectName, String projectDescription)
//    {
//        assertNotNull(result);
//        assertTrue(result instanceof Hashtable);
//        Hashtable<String, Object> details = (Hashtable<String, Object>) result;
//
//        // check that the necessary entries exist.
//        assertEquals(6, details.size());
//        assertNotNull(details.get("id"));
//        assertEquals(projectName, details.get("name"));
//        assertEquals(projectDescription, details.get("description"));
//        assertEquals(PROJECT_URL, details.get("url"));
//        assertEquals("cvs", details.get("scm"));
//        assertEquals("ant", details.get("type"));
//    }
//
//    public void testGetProjectBuildSpecifications() throws IOException, XmlRpcException
//    {
//        String projectName = createProject();
//
//        Object result = xmlRpcClient.execute("RemoteApi.getProjectBuildSpecifications", getVector(adminToken, projectName));
//        assertNotNull(result);
//        assertTrue(result instanceof Vector);
//
//        Vector<String> names = (Vector<String>) result;
//        assertEquals(1, names.size());
//        assertEquals("default", names.get(0));
//    }
//
//    public void testGetScm() throws IOException, XmlRpcException
//    {
//        // create a project with a specific name.
//        String projectName = createProject();
//
//        Object result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));
//        assertNotNull(result);
//        assertTrue(result instanceof Hashtable);
//
//        Hashtable<String, Object> details = (Hashtable<String, Object>) result;
//        assertEquals(4, details.size());
//
//        assertEquals(":pserver:someone@localhost:/cvsroot", details.get("root"));
//        assertEquals("project", details.get("module"));
//        assertEquals("false", details.get("monitor"));
//        assertEquals("0", details.get("quietPeriod"));
//    }
//
//    public void testEditScm() throws IOException, XmlRpcException
//    {
//        // create a project with a specific name.
//        String projectName = createProject();
//
//        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
//        scmDetails.put("root", ":pserver:me@localhost:/cvsroot");
//        scmDetails.put("module", "myProject");
//        scmDetails.put("monitor", "true");
//        scmDetails.put("quietPeriod", "55555");
//
//        Object result = xmlRpcClient.execute("RemoteApi.editScm", getVector(adminToken, projectName, scmDetails));
//        assertEquals(Boolean.TRUE, result);
//
//        result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));
//
//        Hashtable<String, Object> details = (Hashtable<String, Object>) result;
//        assertEquals(4, details.size());
//
//        assertEquals(":pserver:me@localhost:/cvsroot", details.get("root"));
//        assertEquals("myProject", details.get("module"));
//        assertEquals("true", details.get("monitor"));
//        assertEquals("55555", details.get("quietPeriod"));
//
//        // return the monitor to false.
//        scmDetails.put("monitor", "false");
//
//        result = xmlRpcClient.execute("RemoteApi.editScm", getVector(adminToken, projectName, scmDetails));
//        assertEquals(Boolean.TRUE, result);
//
//        result = xmlRpcClient.execute("RemoteApi.getScm", getVector(adminToken, projectName));
//        details = (Hashtable<String, Object>) result;
//        assertEquals("false", details.get("monitor"));
//
//    }
//
//    private String createProject() throws IOException, XmlRpcException
//    {
//        return createProject("ant", "buildFile", "build.xml");
//    }
//
//    private String createProject(String type, String... typeArgs) throws IOException, XmlRpcException
//    {
//        String project = String.format("project-%s", RandomUtils.randomString(4));
//
//        Hashtable<String, Object> projectDetails = new Hashtable<String, Object>();
//        projectDetails.put("name", project);
//        projectDetails.put("description", PROJECT_DESCRIPTION);
//        projectDetails.put("url", PROJECT_URL);
//
//        Hashtable<String, Object> scmDetails = new Hashtable<String, Object>();
//        scmDetails.put("type", "cvs");
//        scmDetails.put("root", ":pserver:someone@localhost:/cvsroot");
//        scmDetails.put("module", "project");
//
//        Hashtable<String, Object> typeDetails = new Hashtable<String, Object>();
//        typeDetails.put("type", type);
//        for(int i = 0; i < typeArgs.length; i += 2)
//        {
//            typeDetails.put(typeArgs[i], typeArgs[i + 1]);
//        }
//
//        Object result = xmlRpcClient.execute("RemoteApi.createProject", getVector(adminToken, projectDetails, scmDetails, typeDetails));
//        assertEquals(Boolean.TRUE, result);
//
//        return project;
//    }
}
