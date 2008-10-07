package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.api.ScmFile;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class ExpectedTestResultsTest extends TestCase
{
    // need to verify that the expected test results are what we actually expect.

    private ExpectedTestResults results;
    private List<Revision> revisions;

    protected void setUp() throws Exception
    {
        super.setUp();

        revisions = Arrays.asList(newRevision("1"),
                newRevision("2"),
                newRevision("3"),
                newRevision("4"),
                newRevision("5"));
        results = new ExpectedTestResults(revisions);
    }

    protected void tearDown() throws Exception
    {
        revisions = null;
        results = null;

        super.tearDown();
    }

    private Revision newRevision(String revisionString)
    {
        return new Revision(null, null, null, revisionString);
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

    public void testGetAggregtedChanges()
    {
        Changelist changelist = results.getAggregatedChanges(revisions.get(0), revisions.get(4));
        assertNotNull(changelist);
        assertNull(changelist.getRevision()); // this is a virtual changelist, no directly associated revision exists.

        List<Change> changes = changelist.getChanges();
        assertEquals(6, changes.size());

        Map<String, Change.Action> expectedActions = new HashMap<String, Change.Action>();
        expectedActions.put("project/README.txt", Change.Action.EDIT);
        expectedActions.put("project/build.xml", Change.Action.ADD);
        expectedActions.put("project/src/Src.java", Change.Action.DELETE);
        expectedActions.put("project/src/com/Com.java", Change.Action.ADD);
        expectedActions.put("project/src/com/package.properties", Change.Action.ADD);
        expectedActions.put("project/test/Test.java", Change.Action.EDIT);

        for (Change change : changes)
        {
            assertEquals(expectedActions.get(change.getFilename()), change.getAction());
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

        // temp hack - need to mark Change instances as persisted so that a value comparison is done.
        // It is all to do with entity equality and persistence.
        for (Change change : changelist.getChanges())
        {
            change.setId(1);
        }

        assertTrue(changelist.getChanges().contains(createPersistentChange("project", null, Change.Action.ADD, true)));
        assertTrue(changelist.getChanges().contains(createPersistentChange("project/src", null, Change.Action.ADD, true)));
        assertTrue(changelist.getChanges().contains(createPersistentChange("project/test", null, Change.Action.ADD, true)));
    }

    private Change createPersistentChange(String filename, String revisionString, Change.Action action, boolean isDir)
    {
        Change change = new Change(filename, revisionString, action, isDir);
        change.setId(1);
        return change;
    }
}
