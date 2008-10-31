package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.plugins.PluginPaths;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.File;
import java.io.IOException;

public class PluginsArchiveTest extends PulseTestCase
{
    private File tmp;
    private File pluginBase;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        pluginBase = new File(tmp, "plugins");
        assertTrue(pluginBase.mkdirs());
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        pluginBase = null;

        super.tearDown();
    }

    public void testRoundtrip() throws IOException, ArchiveException
    {
        assertTrue(new File(pluginBase, "plugin-1.jar").createNewFile());
        assertTrue(new File(pluginBase, "plugin-2.jar").createNewFile());
        assertTrue(new File(pluginBase, "plugin-3.jar").createNewFile());
        assertTrue(new File(pluginBase, "plugin-registry.xml").createNewFile());

        PluginPaths paths = mock(PluginPaths.class);
        stub(paths.getPluginStorageDir()).toReturn(pluginBase);

        File archiveDir = new File(tmp, "archive");

        PluginsArchive archiver = new PluginsArchive();
        archiver.setPluginPaths(paths);
        archiver.backup(archiveDir);

        File newPluginBase = new File(tmp, "plugins-restore");
        stub(paths.getPluginStorageDir()).toReturn(newPluginBase);
        archiver.restore(archiveDir);

        assertDirectoriesEqual(pluginBase, newPluginBase);
    }
}
