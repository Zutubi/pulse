package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;

public class DataDirectoryArchiveTest extends PulseTestCase
{
    private File tmp;
    private File data;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();
        data = new File(tmp, "data");

        setupDataDirectory(data);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        tmp = null;
        data = null;

        super.tearDown();
    }

    private void setupDataDirectory(File data) throws IOException
    {
        assertTrue(data.mkdirs());
        
        File pulseProperties = new File(data, "pulse.config.properties");
        assertTrue(pulseProperties.createNewFile());

        File userConfigRoot = new File(data, "config");
        assertTrue(userConfigRoot.mkdirs());
        assertTrue(new File(userConfigRoot, "a.txt").createNewFile());
        assertTrue(new File(userConfigRoot, "b.txt").createNewFile());
        assertTrue(new File(userConfigRoot, "templates").mkdirs());
        assertTrue(new File(userConfigRoot, "templates/c.txt").createNewFile());
        assertTrue(new File(userConfigRoot, "templates/d.txt").createNewFile());
    }

    public void testContentsOfConfigDirectoryHandled() throws IOException, ArchiveException
    {
        File restoreData = new File(tmp, "data-restore");

        backupAndRestore(restoreData);

        assertDirectoriesEqual(restoreData, data);
    }

    public void testEnsureDatabasePropertiesNotOverriden() throws IOException, ArchiveException
    {
        File restoreData = new File(tmp, "data-restore");

        File databaseProperties = new File(data, "config/database.properties");
        IOUtils.writeToFile(databaseProperties, new ByteArrayInputStream("backup".getBytes()));

        File newDatabaseProperties = new File(restoreData, "config/database.properties");
        assertTrue(newDatabaseProperties.getParentFile().mkdirs());
        IOUtils.writeToFile(newDatabaseProperties, new ByteArrayInputStream("new".getBytes()));

        backupAndRestore(restoreData);

        assertTrue(newDatabaseProperties.isFile());
        assertEquals("new", IOUtils.fileToString(newDatabaseProperties));
    }

    private void backupAndRestore(File dataRestore) throws ArchiveException
    {
        MasterUserPaths paths = mock(MasterUserPaths.class);
        stub(paths.getUserConfigRoot()).toReturn(new File(data, "config"));
        stub(paths.getData()).toReturn(data);

        DatabaseConsole console = mock(DatabaseConsole.class);
        stub(console.isEmbedded()).toReturn(false);

        DataDirectoryArchive archive = new DataDirectoryArchive();
        archive.setUserPaths(paths);
        archive.setDatabaseConsole(console);

        File archiveBase = new File(tmp, "archive");
        archive.backup(archiveBase);

        stub(paths.getUserConfigRoot()).toReturn(new File(dataRestore, "config"));
        stub(paths.getData()).toReturn(dataRestore);

        archive.restore(archiveBase);
    }
}
