package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

public class CleanupAllCapturedWorkingCopiesUpgradeTaskTest extends PulseTestCase
{
    private CleanupAllCapturedWorkingCopiesUpgradeTask upgradeTask;

    private File data;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        data = createTempDirectory();

        MasterConfigurationManager configurationManager = mock(MasterConfigurationManager.class);
        doReturn(data).when(configurationManager).getDataDirectory();
        
        upgradeTask = new CleanupAllCapturedWorkingCopiesUpgradeTask();
        upgradeTask.setConfigurationManager(configurationManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(data);

        super.tearDown();
    }

    public void testEmptyProjectsDirectory() throws TaskException
    {
        mkdir("projects");
        upgradeTask.execute();
        assertExists("projects");
    }

    public void testProjectsWithoutWorkingCopy() throws TaskException, IOException
    {
        mkdir("projects/projectName/000001/123452/output");
        mkdir("projects/projectName/000001/123452/features");
        mkfile("projects/projectName/000001/123452/recipe.log");

        upgradeTask.execute();

        assertExists("projects/projectName/000001/123452/output");
        assertExists("projects/projectName/000001/123452/features");
        assertExists("projects/projectName/000001/123452/recipe.log");
    }

    public void testProjectsWithWorkingCopy() throws TaskException, IOException
    {
        mkdir("projects/projectName/000001/123452/base");
        mkdir("projects/projectName/000001/123452/output");
        mkdir("projects/projectName/000001/123452/features");
        mkfile("projects/projectName/000001/123452/recipe.log");

        upgradeTask.execute();

        assertNotExists("projects/projectName/000001/123452/base");
        assertExists("projects/projectName/000001/123452/output");
        assertExists("projects/projectName/000001/123452/features");
        assertExists("projects/projectName/000001/123452/recipe.log");
    }

    private void mkdir(String dir)
    {
        assertTrue(new File(data, dir).mkdirs());
    }

    private void mkfile(String file) throws IOException
    {
        File f = new File(data, file);
        if (!f.getParentFile().exists())
        {
            assertTrue(f.getParentFile().mkdirs());
        }
        assertTrue(f.createNewFile());
    }

    private void assertExists(String str)
    {
        assertTrue(new File(data, str).exists());
    }

    private void assertNotExists(String str)
    {
        assertTrue(!new File(data, str).exists());
    }
}
