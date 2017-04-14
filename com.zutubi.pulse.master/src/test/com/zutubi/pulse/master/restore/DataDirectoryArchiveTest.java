/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.restore;

import com.google.common.io.Files;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.junit.IOAssertions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class DataDirectoryArchiveTest extends PulseTestCase
{
    private File tmp;
    private File data;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
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

        IOAssertions.assertDirectoriesEqual(restoreData, data);
    }

    public void testEnsureDatabasePropertiesNotOverriden() throws IOException, ArchiveException
    {
        File restoreData = new File(tmp, "data-restore");

        File databaseProperties = new File(data, "config/database.properties");
        Files.write("backup", databaseProperties, Charset.defaultCharset());

        File newDatabaseProperties = new File(restoreData, "config/database.properties");
        assertTrue(newDatabaseProperties.getParentFile().mkdirs());
        Files.write("new", newDatabaseProperties, Charset.defaultCharset());

        backupAndRestore(restoreData);

        assertTrue(newDatabaseProperties.isFile());
        assertEquals("new", Files.toString(newDatabaseProperties, Charset.defaultCharset()));
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
