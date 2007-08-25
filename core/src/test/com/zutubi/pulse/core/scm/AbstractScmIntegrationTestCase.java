package com.zutubi.pulse.core.scm;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.text.ParseException;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;

/**
 *
 *
 */
public abstract class AbstractScmIntegrationTestCase extends TestCase
{
    protected File workingDir;

    protected ScmClient client;
    protected ExpectedTestResults testData;
    protected String prefix;

    protected void setUp() throws Exception
    {
        super.setUp();

        workingDir = FileSystemUtils.createTempDir("dir", null, null);
    }

    protected void tearDown() throws Exception
    {
        assertTrue(FileSystemUtils.rmdir(workingDir));
        workingDir = null;

        super.tearDown();
    }

    protected void assertFilesExist(List<ScmFile> files)
    {
        for (ScmFile file : files)
        {
            //FIXME: check if file represents file or directory, and assert accordingly.
            assertTrue(new File(workingDir, file.getPath()).exists());
        }
    }

    protected void assertRevisionsEqual(List<Revision> expectedRevisions, List<Revision> revisions)
    {
        assertNotNull(revisions);
        assertEquals(expectedRevisions.size(), revisions.size());
        for (int i = 0; i < expectedRevisions.size(); i++)
        {
            assertEquals(expectedRevisions.get(i), revisions.get(i));
        }
    }

    protected void assertChangelistsEqual(List<Changelist> expectedChangeLists, List<Changelist> changelists)
    {
        assertEquals(expectedChangeLists.size(), changelists.size());

        for (int i = 0; i < expectedChangeLists.size(); i++)
        {
            Changelist expectedChangelist = expectedChangeLists.get(i);
            Changelist changelist = changelists.get(i);

            assertEquals(expectedChangelist, changelist);
        }
    }

    private void assertEquals(Changelist expectedChangelist, Changelist changelist)
    {
        assertEquals(expectedChangelist.getRevision(), changelist.getRevision());

        // verify the changes are alike.
        List<Change> expectedChanges = expectedChangelist.getChanges();
        List<Change> changes = changelist.getChanges();

        assertChangesEqual(expectedChanges, changes);
    }

    protected void assertChangesEqual(List<Change> expectedChanges, List<Change> changes)
    {
        assertEquals(expectedChanges.size(), changes.size());

/*
        // compare changes - filename and actions.
        Map<String, Change.Action> expectedActions = new HashMap<String, Change.Action>();
        for (Change expectedChange : expectedChanges)
        {
            expectedActions.put(expectedChange.getFilename(), expectedChange.getAction());
        }
        for (Change change : changes)
        {
            assertTrue(expectedActions.containsKey(change.getFilename()));
//  it would be nice for the actions to be the same, but it seems that at lease cvs reports them differently to whats expected
//          assertEquals(expectedActions.get(change.getFilename()), change.getAction());
        }
*/
        // expected change paths are not necessarily exactly the same as the reported paths. At the very least, they
        // will match the end of a path.
        for (Change change : changes)
        {
            String filename = change.getFilename();
            boolean found = false;
            for (Change expectedChange : expectedChanges)
            {
                if (filename.endsWith(expectedChange.getFilename()))
                {
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    //---( ScmCapability.PREPARE )---

    public void testPrepareACleanDirectory() throws ScmException, ParseException
    {
        ScmContext context = new ScmContext();
        context.setRevision(Revision.HEAD);
        context.setDir(workingDir);

        RecordingScmEventHandler handler = new RecordingScmEventHandler();
//        Revision revision = client.prepare(context, handler);
        Revision revision = client.checkout(context, handler);

        // assert the revision returned is the latest revision
        assertEquals(testData.getLatestRevision(), revision);

        // verify the contents of the output directory.
        // - do we also want to check that only the expected files are there? This is a little more difficult since
        //   scm administration files will also be present...
        assertFilesExist(testData.getFilesFor(revision));

        Changelist expectedChangelist = testData.getAggregatedChanges(null, revision);
        assertChangesEqual(expectedChangelist.getChanges(), handler.getChanges());

/*
        // these properties are specific to the cvs implementation...
        Properties scmProperties = context.getProperties();
        assertEquals(null, scmProperties.getProperty("cvs.branch"));
        assertEquals(CVSROOT, scmProperties.getProperty("cvs.root"));
        assertEquals(MODULE, scmProperties.getProperty("cvs.module"));
*/
    }

    public void testPrepareAnExistingDirectory() throws ScmException
    {
        Revision initialRevision = testData.getRevision(0);

        ScmContext context = new ScmContext();
        context.setRevision(initialRevision);
        context.setDir(workingDir);

        RecordingScmEventHandler handler = new RecordingScmEventHandler();
//        client.prepare(context, handler);
        client.checkout(context, handler);

        // now for the update.

        handler.reset();

        Revision requestedRevision = testData.getRevision(1);

        context = new ScmContext();
        context.setRevision(requestedRevision);
        context.setDir(workingDir);

//        Revision actualRevision = client.prepare(context, handler);
        Revision actualRevision = client.update(context, handler);
        assertEquals(requestedRevision, actualRevision);

        Changelist expectedChangelist = testData.getAggregatedChanges(initialRevision, requestedRevision);

        assertFilesExist(testData.getFilesFor(requestedRevision));
        assertChangesEqual(expectedChangelist.getChanges(), handler.getChanges());
    }

    public void testRetrieveFile() throws ScmException, IOException
    {
        InputStream input = null;
        try
        {
            input = client.retrieve(prefix + "project/src/com/package.properties", null);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.joinStreams(input, output);
            assertEquals("key=value", output.toString());
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public void testRetrieveFileOfSpecifiedRevision() throws ScmException, IOException
    {
        String path = prefix + "project/test/Test.java";

        InputStream input = null;
        try
        {
            input = client.retrieve(path, testData.getRevision(3));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.joinStreams(input, output);
            assertFalse(output.toString().contains("testSomethingElse"));

            IOUtils.close(input);

            input = client.retrieve(path, Revision.HEAD);
            output = new ByteArrayOutputStream();
            IOUtils.joinStreams(input, output);
            assertTrue(output.toString().contains("testSomethingElse"));
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public void testRetrieveDirectory() throws ScmException, IOException
    {
        String path = prefix + "project/test/";

        InputStream input = null;
        try
        {
            input = client.retrieve(path, Revision.HEAD);
            fail();
        }
        catch (ScmException e)
        {
            // error messages between scms will be different - should we force a standardisation?
            //assertTrue(e.getMessage().contains("Path references a directory"));
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    public void testRetrieveNonExistantFile() throws ScmException
    {
        // FIXME: using full repository path rather than data path.
        String path = prefix + "project/does/not/exist.txt";

        InputStream input = null;
        try
        {
            input = client.retrieve(path, Revision.HEAD);
            fail();
        }
        catch (ScmException e)
        {
            //FIXME: make this error message a little more helpful.
            // error messages between scms will be different - should we force a standardisation?
            //e.printStackTrace();
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    //---( ScmCapability.BROWSE )---

    public void testBrowse() throws ScmException
    {
        // FIXME: using full repository path rather than data path.
        List<ScmFile> listing = client.browse(prefix + "project", null);

        List<ScmFile> expectedListing = this.testData.browse(prefix + "project");
        assertEquals(expectedListing.size(), listing.size());

        for (ScmFile file : expectedListing)
        {
            assertTrue(listing.contains(file));
        }

        listing = client.browse(prefix + "project/test", null);
        expectedListing = this.testData.browse(prefix + "project/test");
        assertEquals(expectedListing.size(), listing.size());

        for (ScmFile file : expectedListing)
        {
            assertTrue(listing.contains(file));
        }
    }

    public void testAttemptingToBrowseFile() throws ScmException
    {
        // FIXME: using full repository path rather than data path.
        List<ScmFile> listing = client.browse(prefix + "project/README.txt", null);
        List<ScmFile> expectedListing = this.testData.browse(prefix + "project/README.txt");
        assertEquals(expectedListing.size(), listing.size());

        for (ScmFile file : expectedListing)
        {
            assertTrue(listing.contains(file));
        }

        // this is in line with the standard behaviour for ls type commands, which return the file that you
        // listed. However, is this actually what we want?
    }

    //---( ScmCapability.MONITOR )---

    public void testGetLatestRevision() throws ScmException
    {
        Revision revision = client.getLatestRevision();
        assertEquals(testData.getRevision(4), revision);
    }

    public void testGetRevisionsFromXToY() throws ScmException
    {
        Revision x = testData.getRevision(0);
        Revision y = testData.getRevision(4);

        List<Revision> revisions = client.getRevisions(x, y);
        List<Revision> expectedRevisions = testData.getRevisions(x, y);
        assertRevisionsEqual(expectedRevisions, revisions);
    }

    public void testGetRevisionsFromX() throws ScmException
    {
        Revision x = testData.getRevision(2);

        List<Revision> revisions = client.getRevisions(x, null);
        List<Revision> expectedRevisions = testData.getRevisions(x, null);
        assertRevisionsEqual(expectedRevisions, revisions);
    }

/*
// no specifying a lower bound on the lookup is problematic and not practical? Do we ever need such a call?
    public void testGetRevisionsToY() throws ScmException
    {
        Revision y = testData.getRevision(2);

        List<Revision> revisions = client.getRevisions(null, y);
        List<Revision> expectedRevisions = testData.getRevisions(null, y);
        assertRevisionsEqual(expectedRevisions, revisions);
    }
*/

    //---( ScmCapability.CHANGESET )---

    public void testChangesetFromXToY() throws ScmException
    {
        Revision x = testData.getRevision(0);
        Revision y = testData.getRevision(1);

        List<Changelist> changelists = client.getChanges(x, y);
        List<Changelist> expectedChangeLists = testData.getChanges(x, y);
        assertChangelistsEqual(expectedChangeLists, changelists);

        x = testData.getRevision(1);
        y = testData.getRevision(2);

        changelists = client.getChanges(x, y);
        expectedChangeLists = testData.getChanges(x, y);
        assertChangelistsEqual(expectedChangeLists, changelists);

        x = testData.getRevision(2);
        y = testData.getRevision(4);

        changelists = client.getChanges(x, y);
        expectedChangeLists = testData.getChanges(x, y);
        assertChangelistsEqual(expectedChangeLists, changelists);

        //FIXME: need to decide what we want to happen here. Do we want the changelist
        // that represents the change from the THIRD_REVISION to the FOURTH_REVISION,
        // or do we say exclude x, include y so since they are the same, we return nothing?

        x = testData.getRevision(4);
        y = testData.getRevision(4);

        changelists = client.getChanges(x, y);
        assertEquals(0, changelists.size());
    }

    public void testChangesetFromX() throws ScmException
    {
        Revision x = testData.getRevision(2);

        List<Changelist> changelists = client.getChanges(x, null);
        List<Changelist> expectedChangeLists = testData.getChanges(x, null);
        assertChangelistsEqual(expectedChangeLists, changelists);
    }

/*
// no specifying a lower bound on the lookup is problematic and not practical? Do we ever need such a call?
    public void testChangesetToY() throws ScmException
    {
        Revision y = testData.getRevision(2);

        List<Changelist> changelists = client.getChanges(null, y);
        List<Changelist> expectedChangeLists = testData.getChanges(null, y);
        assertChangelistsEqual(expectedChangeLists, changelists);
    }
*/

    //---( ScmCapability.PATCH )---

/*
    public void testGetStatusOfChangedFile() throws ScmException, IOException
    {
        doCheckoutToHead();

        // modify a file.
        File propertyFile = new File(workingDir, prefix + "project/src/com/package.properties");
        assertTrue(propertyFile.isFile());
        Utils.write(new Properties(), propertyFile);

        // we need to take the status from within the working directory.
        WorkingCopyStatus status = client.getStatus(new File(workingDir, prefix + "project"));
        assertNotNull(status);
        assertTrue(status.hasChanges());
        assertEquals(1, status.getChanges().size());

        FileStatus fileStatus = status.getFileStatus("src/com/package.properties");
        assertNotNull(fileStatus);
        assertEquals(FileStatus.State.MODIFIED, fileStatus.getState());
        assertEquals("src/com/package.properties", fileStatus.getPath());
        assertEquals(prefix + "project/src/com/package.properties", fileStatus.getTargetPath());
    }

    public void testGetStatusOfUnChangedFile() throws ScmException
    {
        doCheckoutToHead();

        // we need to take the status from within the working directory.
        WorkingCopyStatus status = client.getStatus(new File(workingDir, prefix));
        assertNotNull(status);
        assertFalse(status.hasChanges());
    }

    public void testGetStatusOfDeletedFile() throws ScmException, IOException
    {
        doCheckoutToHead();

        // delete the file
        // FIXME: need to mark the file for deletion as well, otherwise this is just a missing local file that needs to be checked out..
        String filename = prefix + "project/README.txt";
        assertTrue(new File(workingDir, filename).delete());

        // update cvs ...
        File entries = new File(workingDir, prefix + "project/CVS/Entries");
        String newEntriesContent = "/build.xml/1.1/Sun Jul 29 12:35:08 2007//\n" +
                "D/src////\n" +
                "D/test////\n" +
                "/README.txt/-1.5/dummy timestamp//";
        Utils.write(newEntriesContent, entries);

        // we need to run the status within the working copy.
        WorkingCopyStatus status = client.getStatus(new File(workingDir, "integration-test"));
        assertTrue(status.hasChanges());
        assertEquals(1, status.getChanges().size());

        FileStatus fileStatus = status.getFileStatus("project/README.txt");
        assertNotNull(fileStatus);
        assertEquals(FileStatus.State.DELETED, fileStatus.getState());
        assertEquals("project/README.txt", fileStatus.getPath());
        assertEquals(prefix + "project/README.txt", fileStatus.getTargetPath());
    }

    public void testGetStatusOfAddedFile()
    {
        // how do we mark a file as 'added' ? the ScmClient interface does not support this at the moment,
        // and we do not want to be modifying the Scm itself...
        fail("nyi");
    }
*/

    private void doCheckoutToHead() throws ScmException
    {
        ScmContext context = new ScmContext();
        context.setRevision(Revision.HEAD);
        context.setDir(workingDir);

//        Revision revison = client.prepare(context, null);
        Revision revison = client.checkout(context, null);
        assertEquals(testData.getRevision(4), revison);
    }

    //---( ScmCapability.TAG )---

/*
    public void testTagging() throws ScmException
    {
        // always use the same tag since we currently do not support deleting of a tag.
        String tag = "TAG";

        // need two revisions to work with. Tag one, verify, tag the other verify.

//        client.tag(null, tag, false);

        fail("nyi");
    }
*/

}
