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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmFile;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class ExpectedTestResultsTest extends ZutubiTestCase
{
    // need to verify that the expected test results are what we actually expect.

    private ExpectedTestResults results;
    private List<Revision> revisions;

    protected void setUp() throws Exception
    {
        super.setUp();

        revisions = Arrays.asList(new Revision("1"),
                new Revision("2"),
                new Revision("3"),
                new Revision("4"),
                new Revision("5"));
        results = new ExpectedTestResults(revisions);
    }

    protected void tearDown() throws Exception
    {
        revisions = null;
        results = null;

        super.tearDown();
    }

    public void testGetFilesForRevision1()
    {
        List<ScmFile> files = results.getFilesFor(revisions.get(0));
        assertNotNull(files);
        assertEquals(3, files.size());
        assertTrue(files.contains(new ScmFile("project/README.txt")));
        assertTrue(files.contains(new ScmFile("project/src/Src.java")));
        assertTrue(files.contains(new ScmFile("project/test/Test.java")));
    }

    public void testGetFilesForRevision2()
    {
        List<ScmFile> files = results.getFilesFor(revisions.get(1));
        assertNotNull(files);
        assertEquals(6, files.size());
        assertTrue(files.contains(new ScmFile("project/README.txt")));
        assertTrue(files.contains(new ScmFile("project/build.xml")));
        assertTrue(files.contains(new ScmFile("project/src/Src.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/Com.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/package.properties")));
        assertTrue(files.contains(new ScmFile("project/test/Test.java")));
    }

    public void testGetFilesForRevision3()
    {
        List<ScmFile> files = results.getFilesFor(revisions.get(2));
        assertNotNull(files);
        assertEquals(6, files.size());
        assertTrue(files.contains(new ScmFile("project/README.txt")));
        assertTrue(files.contains(new ScmFile("project/build.xml")));
        assertTrue(files.contains(new ScmFile("project/src/Src.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/Com.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/package.properties")));
        assertTrue(files.contains(new ScmFile("project/test/Test.java")));
    }

    public void testGetFilesForRevision4()
    {
        List<ScmFile> files = results.getFilesFor(revisions.get(3));
        assertNotNull(files);
        assertEquals(5, files.size());
        assertTrue(files.contains(new ScmFile("project/README.txt")));
        assertTrue(files.contains(new ScmFile("project/build.xml")));
        assertTrue(files.contains(new ScmFile("project/src/com/Com.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/package.properties")));
        assertTrue(files.contains(new ScmFile("project/test/Test.java")));
    }

    public void testGetFilesForRevision5()
    {
        List<ScmFile> files = results.getFilesFor(revisions.get(4));
        assertNotNull(files);
        assertEquals(5, files.size());
        assertTrue(files.contains(new ScmFile("project/README.txt")));
        assertTrue(files.contains(new ScmFile("project/build.xml")));
        assertTrue(files.contains(new ScmFile("project/src/com/Com.java")));
        assertTrue(files.contains(new ScmFile("project/src/com/package.properties")));
        assertTrue(files.contains(new ScmFile("project/test/Test.java")));
    }

    public void testGetLatestRevision()
    {
        assertEquals(revisions.get(4), results.getLatestRevision());
    }

    public void testChanges()
    {
        List<Changelist> changelists = results.getChanges(null, revisions.get(4));
        assertEquals(5, changelists.size());
        assertEquals(revisions.get(0), changelists.get(0).getRevision());
        assertEquals(revisions.get(1), changelists.get(1).getRevision());
        assertEquals(revisions.get(2), changelists.get(2).getRevision());
        assertEquals(revisions.get(3), changelists.get(3).getRevision());
        assertEquals(revisions.get(4), changelists.get(4).getRevision());

        changelists = results.getChanges(revisions.get(1), revisions.get(3));
        assertEquals(2, changelists.size());
        assertEquals(revisions.get(2), changelists.get(0).getRevision());
        assertEquals(revisions.get(3), changelists.get(1).getRevision());
    }

    public void testGetRevisions()
    {
        List<Revision> revs = results.getRevisions(revisions.get(0), revisions.get(4));
        assertEquals(4, revs.size());
        assertEquals(revisions.get(1), revs.get(0));
        assertEquals(revisions.get(2), revs.get(1));
        assertEquals(revisions.get(3), revs.get(2));
        assertEquals(revisions.get(4), revs.get(3));

        revs = results.getRevisions(revisions.get(1), revisions.get(4));
        assertEquals(3, revs.size());
        assertEquals(revisions.get(2), revs.get(0));
        assertEquals(revisions.get(3), revs.get(1));
        assertEquals(revisions.get(4), revs.get(2));
    }

    public void testGetAggregatedChanges()
    {
        Changelist changelist = results.getAggregatedChanges(revisions.get(0), revisions.get(4));
        assertNotNull(changelist);

        List<FileChange> changes = changelist.getChanges();
        assertEquals(6, changes.size());

        Map<String, FileChange.Action> expectedActions = new HashMap<String, FileChange.Action>();
        expectedActions.put("project/README.txt", FileChange.Action.EDIT);
        expectedActions.put("project/build.xml", FileChange.Action.ADD);
        expectedActions.put("project/src/Src.java", FileChange.Action.DELETE);
        expectedActions.put("project/src/com/Com.java", FileChange.Action.ADD);
        expectedActions.put("project/src/com/package.properties", FileChange.Action.ADD);
        expectedActions.put("project/test/Test.java", FileChange.Action.EDIT);

        for (FileChange change : changes)
        {
            assertEquals(expectedActions.get(change.getPath()), change.getAction());
        }
    }

    public void testBrowseRoot()
    {
        List<ScmFile> listing = results.browse(null);
        assertEquals(1, listing.size());
        assertTrue(listing.contains(new ScmFile("project", true)));
    }

    public void testBrowseDirectory()
    {
        List<ScmFile> listing = results.browse("project");
        assertEquals(4, listing.size());
        assertTrue(listing.contains(new ScmFile("project/README.txt")));
        assertTrue(listing.contains(new ScmFile("project/build.xml")));
        assertTrue(listing.contains(new ScmFile("project/src", true)));
        assertTrue(listing.contains(new ScmFile("project/test", true)));
    }

    public void testBrowseWithVersionedDirectorySupport()
    {
        results.setVersionDirectorySupport(true);

        List<ScmFile> listing = results.browse("project");
        assertEquals(4, listing.size());
        assertTrue(listing.contains(new ScmFile("project/README.txt")));
        assertTrue(listing.contains(new ScmFile("project/build.xml")));
        assertTrue(listing.contains(new ScmFile("project/src", true)));
        assertTrue(listing.contains(new ScmFile("project/test", true)));
    }

    public void testBrowseFile()
    {
        List<ScmFile> listing = results.browse("project/README.txt");
        assertEquals(1, listing.size());
        assertTrue(listing.contains(new ScmFile("project/README.txt")));
    }

    public void testBrowseFileWithVersionedDirectorySupport()
    {
        results.setVersionDirectorySupport(true);

        List<ScmFile> listing = results.browse("project/README.txt");
        assertEquals(1, listing.size());
        assertTrue(listing.contains(new ScmFile("project/README.txt")));
    }

    public void testBrowseEmptyDirectories()
    {
        // do not have any of these in the test data as yet, so no test for this.
    }

    public void testVersionedDirectorySupport()
    {
        Changelist changelist = results.getChange(revisions.get(0));
        assertEquals(3, changelist.getChanges().size());

        results.setVersionDirectorySupport(true);
        changelist = results.getChange(revisions.get(0));
        assertEquals(6, changelist.getChanges().size());

        assertTrue(changelist.getChanges().contains(new FileChange("project", null, FileChange.Action.ADD, true)));
        assertTrue(changelist.getChanges().contains(new FileChange("project/src", null, FileChange.Action.ADD, true)));
        assertTrue(changelist.getChanges().contains(new FileChange("project/test", null, FileChange.Action.ADD, true)));
    }
}
