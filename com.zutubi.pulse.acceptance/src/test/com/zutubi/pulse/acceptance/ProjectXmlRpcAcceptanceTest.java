package com.zutubi.pulse.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests configuration of projects via the remote API.
 */
public class ProjectXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final long BUILD_TIMEOUT = 90000;

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
        assertDefaultCleanups(globalProject);
        assertDefaultOptions(globalProject);
    }

    public void testInsertProject() throws Exception
    {
        String projectName = randomName();

        String path = insertSimpleProject(projectName);
        assertEquals("projects/" + projectName, path);

        Hashtable<String, Object> createdProject = call("getConfig", path);
        assertProject(createdProject, projectName);
        assertDefaultCleanups(createdProject);
        assertDefaultOptions(createdProject);

        Hashtable<String, Object> createdScm = (Hashtable<String, Object>) createdProject.get("scm");
        assertNotNull(createdScm);
        assertEquals("zutubi.subversionConfig", createdScm.get(SYMBOLIC_NAME_KEY));
        assertEquals(Constants.TRIVIAL_ANT_REPOSITORY, createdScm.get("url"));
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
        assertDefaultCleanups(editedProject);
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

    public void testSetNextBuildNumber() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);

        try
        {
            xmlRpcHelper.call("setNextBuildNumber", projectName, "haha");
            fail("Can't use invalid build number");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("NumberFormatException: For input string: \"haha\""));
        }

        try
        {
            xmlRpcHelper.setNextBuildNumber(projectName, 0);
            fail("Can't decrease build number");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("The given build number '0' is not large enough (build numbers must always increase)"));
        }

        xmlRpcHelper.setNextBuildNumber(projectName, 22);
        int id = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);
        assertEquals(22, id);
    }

    private void assertProject(Hashtable<String, Object> struct, String name)
    {
        assertEquals("zutubi.projectConfig", struct.get(SYMBOLIC_NAME_KEY));
        assertEquals(name, struct.get("name"));
    }

    private void assertDefaultCleanups(Hashtable<String, Object> project)
    {
        Hashtable<String, Object> cleanups = (Hashtable<String, Object>) project.get("cleanup");
        assertNotNull(cleanups);
        assertDefaultCleanup(cleanups);
    }

    private void assertDefaultCleanup(Hashtable<String, Object> cleanups)
    {
        Hashtable<String, Object> defaultCleanup = (Hashtable<String, Object>) cleanups.get("default");
        assertNotNull(defaultCleanup);
        assertEquals("zutubi.cleanupConfig", defaultCleanup.get(SYMBOLIC_NAME_KEY));
        assertEquals("default", defaultCleanup.get("name"));
        assertEquals(10, defaultCleanup.get("retain"));
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
}
