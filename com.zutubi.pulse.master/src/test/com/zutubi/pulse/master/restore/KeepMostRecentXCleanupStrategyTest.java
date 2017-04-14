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

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.File;
import java.io.IOException;

public class KeepMostRecentXCleanupStrategyTest extends PulseTestCase
{
    private File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);

        super.tearDown();
    }

    public void testEmptyCandidateList()
    {
        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(10);
        File[] targets = strategy.getCleanupTargets(null);
        assertNotNull(targets);
        assertEquals(0, targets.length);

        targets = strategy.getCleanupTargets(new File[0]);
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListLessThanX() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(10);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListEqualToX() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(2);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(0, targets.length);
    }

    public void testCandidateListGreaterThanX() throws IOException
    {
        createCandidateFiles(3);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(2);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(1, targets.length);
        assertEquals("backup-0", targets[0].getName());
    }

    public void testXEqualsZero() throws IOException
    {
        createCandidateFiles(2);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(0);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(2, targets.length);
    }

    public void testMostRecentReturned() throws IOException
    {
        createCandidateFiles(5);

        BackupCleanupStrategy strategy = new KeepMostRecentXCleanupStrategy(3);
        File[] targets = strategy.getCleanupTargets(tmp.listFiles());
        assertNotNull(targets);
        assertEquals(2, targets.length);
        assertEquals("backup-0", targets[0].getName());
        assertEquals("backup-1", targets[1].getName());
    }

    private void createCandidateFiles(int x) throws IOException
    {
        for (int i = 0; i < x; i++)
        {
            createTmpFile("backup-" + i).setLastModified(System.currentTimeMillis() - (x - i) * 1000);
        }
    }

    private File createTmpFile(String fileName) throws IOException
    {
        File f = new File(tmp, fileName);
        f.createNewFile();
        return f;
    }

}
