package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupUnit;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.Hashtable;

/**
 * The set of acceptance tests for the projects cleanup configuration.
 */
public class CleanupAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final long BUILD_TIMEOUT = 90000;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();

        super.tearDown();
    }

    public void testCleanupWorkingDirectories() throws Exception
    {
        String name = randomName();
        xmlRpcHelper.insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false);

        setRetainWorkingCopy(name, true);

        addCleanupRule(name, "wd", CleanupWhat.WORKING_DIRECTORIES_ONLY);

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(hasBuildWorkingCopy(name, 1));

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertFalse(hasBuildWorkingCopy(name, 1));
        assertTrue(hasBuildWorkingCopy(name, 2));
    }

    public void testCleanupBuildArtifacts() throws Exception
    {
        String name = randomName();
        xmlRpcHelper.insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false);

        setRetainWorkingCopy(name, true);

        addCleanupRule(name, "ba", CleanupWhat.BUILD_ARTIFACTS);

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(hasBuildDirectory(name, 1));

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertFalse(hasBuildDirectory(name, 1));
        assertTrue(hasBuildDirectory(name, 2));
    }

    public void testCleanupAll() throws Exception
    {
        String name = randomName();
        xmlRpcHelper.insertSimpleProject(name, ProjectManager.GLOBAL_PROJECT_NAME, false);

        setRetainWorkingCopy(name, true);

        addCleanupRule(name, "wb", CleanupWhat.WHOLE_BUILDS);

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertTrue(hasBuild(name, 1));

        xmlRpcHelper.runBuild(name, BUILD_TIMEOUT);
        pause(5); // give cleanup a change to trigger asyncrhonously.

        assertFalse(hasBuild(name, 1));
        assertTrue(hasBuild(name, 2));
    }

    private void setRetainWorkingCopy(String projectName, boolean b) throws Exception
    {
        String optionsPath = "projects/" + projectName + "/options";
        Hashtable<String, Object> data = xmlRpcHelper.getConfig(optionsPath);
        data.put("retainWorkingCopy", b);
        xmlRpcHelper.saveConfig(optionsPath, data, false);
    }

    private void addCleanupRule(String projectName, String name, CleanupWhat what) throws Exception
    {
        Hashtable<String, Object> data = xmlRpcHelper.createDefaultConfig(CleanupConfiguration.class);
        data.put("name", name);
        data.put("retain", 1);
        data.put("unit", CleanupUnit.BUILDS.toString());
        if (what != null)
        {
            data.put("what", what.toString());
        }

        String cleanupPath = "projects/" + projectName + "/cleanup";
        xmlRpcHelper.insertConfig(cleanupPath, data);
    }

    private boolean hasBuild(String projectName, int buildNumber) throws Exception
    {
        return xmlRpcHelper.getBuild(projectName, buildNumber) != null;
    }

    private boolean hasBuildDirectory(String projectName, int buildNumber) throws Exception
    {
        Hashtable<String, Object> projectConfig = xmlRpcHelper.call("getProject", projectName);
        long projectId = Long.parseLong((String) projectConfig.get("id"));

        File data = AcceptanceTestUtils.getDataDirectory();

        File buildDir = new File(data, "projects/" + projectId + "/" + String.format("%08d", buildNumber));
        return buildDir.isDirectory();
    }

    private boolean hasBuildWorkingCopy(String projectName, int buildNumber) throws Exception
    {
        Hashtable<String, Object> projectConfig = xmlRpcHelper.call("getProject", projectName);
        long projectId = Long.parseLong((String) projectConfig.get("id"));

        File data = AcceptanceTestUtils.getDataDirectory();

        File buildDir = new File(data, "projects/" + projectId + "/" + String.format("%08d", buildNumber));
        File stageDir = CollectionUtils.find(buildDir.listFiles(), new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isDirectory();
            }
        });
        return new File(stageDir, "base").isDirectory();
    }

    private void pause(int seconds)
    {
        try
        {
            Thread.sleep(com.zutubi.util.Constants.SECOND * seconds);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }
}
