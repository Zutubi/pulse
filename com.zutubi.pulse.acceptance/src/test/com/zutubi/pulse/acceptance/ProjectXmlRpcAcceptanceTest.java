package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests configuration of projects via the remote API.
 */
public class ProjectXmlRpcAcceptanceTest extends AcceptanceTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testGetGlobal() throws Exception
    {
        Hashtable<String, Object> globalProject = rpcClient.RemoteApi.getConfig("projects/global project template");
        assertProject(globalProject, GLOBAL_PROJECT_NAME);
        assertDefaultOptions(globalProject);
    }

    public void testInsertProject() throws Exception
    {
        String projectName = randomName();

        String path = rpcClient.RemoteApi.insertSimpleProject(projectName);
        assertEquals("projects/" + projectName, path);

        Hashtable<String, Object> createdProject = rpcClient.RemoteApi.getConfig(path);
        assertProject(createdProject, projectName);
        assertDefaultOptions(createdProject);

        Hashtable<String, Object> createdScm = (Hashtable<String, Object>) createdProject.get("scm");
        assertNotNull(createdScm);
        assertEquals("zutubi.subversionConfig", createdScm.get(rpcClient.RemoteApi.SYMBOLIC_NAME_KEY));
        assertEquals(Constants.TRIVIAL_ANT_REPOSITORY, createdScm.get("url"));
        assertEquals("CLEAN_CHECKOUT", createdScm.get("checkoutScheme"));
        assertEquals(false, createdScm.get("monitor"));

        Hashtable<String, Object> createdType = (Hashtable<String, Object>) createdProject.get("type");
        assertNotNull(createdType);
        assertEquals("zutubi.multiRecipeTypeConfig", createdType.get(rpcClient.RemoteApi.SYMBOLIC_NAME_KEY));
        assertEquals("default", createdType.get("defaultRecipe"));

        Hashtable<String, Object> recipes = (Hashtable<String, Object>) createdType.get("recipes");
        assertEquals(1, recipes.size());

        Hashtable<String, Object> recipe = (Hashtable<String, Object>) recipes.get("default");
        assertNotNull(recipe);
        assertEquals("default", recipe.get("name"));

        Hashtable<String, Object> commands = (Hashtable<String, Object>) recipe.get("commands");
        assertEquals(1, commands.size());

        Hashtable<String, Object> command = (Hashtable<String, Object>) commands.get("build");
        assertNotNull(command);
        assertEquals("zutubi.antCommandConfig", command.get(rpcClient.RemoteApi.SYMBOLIC_NAME_KEY));
        assertEquals("build.xml", command.get("buildFile"));
    }

    public void testDeleteProject() throws Exception
    {
        String projectName = randomName();
        String path = rpcClient.RemoteApi.insertSimpleProject(projectName);
        Hashtable<String, Object> createdProject = rpcClient.RemoteApi.getConfig(path);
        assertProject(createdProject, projectName);

        Boolean result = rpcClient.RemoteApi.deleteConfig(path);
        assertTrue(result);
        assertFalse((Boolean) rpcClient.RemoteApi.configPathExists(path));
    }

    public void testEditProject() throws Exception
    {
        String projectName = randomName();
        String path = rpcClient.RemoteApi.insertSimpleProject(projectName);

        String editedName = projectName + " edited";
        String editedPath = "projects/" + editedName;
        String editedUrl = "svn://localhost/test/edited";

        Hashtable<String, Object> createdProject = rpcClient.RemoteApi.getConfig(path);
        createdProject.put("name", editedName);
        Hashtable<String, Object> createdScm = (Hashtable<String, Object>) createdProject.get("scm");
        createdScm.put("url", editedUrl);

        rpcClient.RemoteApi.saveConfig(path, createdProject, true);
        assertFalse((Boolean) rpcClient.RemoteApi.configPathExists(path));

        Hashtable<String, Object> editedProject = rpcClient.RemoteApi.getConfig(editedPath);
        assertProject(editedProject, editedName);
        assertDefaultOptions(editedProject);

        Hashtable<String, Object> editedScm = (Hashtable<String, Object>) editedProject.get("scm");
        assertEquals(editedUrl, editedScm.get("url"));
    }

    public void testListProjects() throws Exception
    {
        Vector<String> projects = rpcClient.RemoteApi.getConfigListing("projects");
        assertTrue(projects.contains(GLOBAL_PROJECT_NAME));
        int sizeBefore = projects.size();

        String name = randomName();
        rpcClient.RemoteApi.insertSimpleProject(name);

        projects = rpcClient.RemoteApi.getConfigListing("projects");
        assertEquals(sizeBefore + 1, projects.size());
        assertTrue(projects.contains(name));
    }

    public void testSetNextBuildNumber() throws Exception
    {
        String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);

        try
        {
            rpcClient.RemoteApi.call("setNextBuildNumber", projectName, "haha");
            fail("Can't use invalid build number");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("NumberFormatException: For input string: \"haha\""));
        }

        try
        {
            rpcClient.RemoteApi.setNextBuildNumber(projectName, 0);
            fail("Can't decrease build number");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("The existing next build number '1' is larger than the given number '0' (build numbers must always increase)"));
        }

        rpcClient.RemoteApi.setNextBuildNumber(projectName, 22);
        int id = rpcClient.RemoteApi.runBuild(projectName);
        assertEquals(22, id);
    }

    private void assertProject(Hashtable<String, Object> struct, String name)
    {
        assertEquals("zutubi.projectConfig", struct.get(rpcClient.RemoteApi.SYMBOLIC_NAME_KEY));
        assertEquals(name, struct.get("name"));
    }

    private void assertDefaultOptions(Hashtable<String, Object> project)
    {
        Hashtable<String, Object> options = (Hashtable<String, Object>) project.get("options");
        assertNotNull(options);
        assertEquals("zutubi.buildOptionsConfig", options.get(rpcClient.RemoteApi.SYMBOLIC_NAME_KEY));
        assertEquals(false, options.get("isolateChangelists"));
        assertEquals(false, options.get("prompt"));
        assertEquals(0, options.get("timeout"));
    }
}
