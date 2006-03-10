package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.test.BobTestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * 
 *
 */
public class CvsClientTest extends BobTestCase
{
    //NOTE: when using the 'local' method, ensure that the 'cvs' command

    private CvsClient cvs;

    private File workdir = null;

    public void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");

        File repositoryRoot = new File(getBobRoot(), "server-core/src/test/com/cinnamonbob/scm/cvs/repository");
//        File repositoryRoot = new File("c:/repository");
        CVSRoot cvsRoot = CVSRoot.parse(":local:" + repositoryRoot.getCanonicalPath());
        cvs = new CvsClient(cvsRoot);

        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory(CvsClientTest.class.getName(), "");
    }

    public void tearDown() throws Exception
    {
        removeDirectory(workdir);

        super.tearDown();
    }

//      is located in your path.

    public void testUpdate() throws Exception
    {
        // check that the contents of a particular file are as 
        // expected after the update.
    }

    public void testChangeDetails() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangeDetails";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());

        Change change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());

        changelist = changes.get(1);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt modified by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.EDIT, change.getAction());
        assertEquals("1.2", change.getRevision());

        changelist = changes.get(2);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file1.txt deleted by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/Attic/file1.txt", change.getFilename());
        assertEquals(Change.Action.DELETE, change.getAction());
        assertEquals("1.3", change.getRevision());

        changelist = changes.get(3);
        assertEquals("daniel", changelist.getUser());
        assertEquals("file2.txt checked in by author a\n", changelist.getComment());
        assertEquals(1, changelist.getChanges().size());

        change = changelist.getChanges().get(0);
        assertEquals("/unit-test/CvsClientTest/testChangeDetails/file2.txt", change.getFilename());
        assertEquals(Change.Action.ADD, change.getAction());
        assertEquals("1.1", change.getRevision());
    }

    public void testChangesByDifferentAuthors() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByDifferentAuthors";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "jason", "file2.txt checked in by author b\n");
        assertEquals(1, changelist.getChanges().size());
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.ADD, "1.1");
    }

    public void testChangesByDifferentAuthorsSameFile() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByDifferentAuthorsSameFile";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(2, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "jason", "file1.txt modified by author b\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
    }

    public void testChangesBySameAuthorOverlappingFiles() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesBySameAuthorOverlappingFiles";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "file3.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file2.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.EDIT, "1.2");

        changelist = changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt and file 2.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.3");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.EDIT, "1.3");
    }

    public void testChangesByOverlappingCommits() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesByOverlappingCommits";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and file3.txt and file4.txt are checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "file3.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(3), "file4.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "x\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file3.txt", Change.Action.EDIT, "1.2");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "jason", "y\n");
        assertChangeValues(changelist.getChanges().get(0), "file2.txt", Change.Action.EDIT, "1.2");
        assertChangeValues(changelist.getChanges().get(1), "file4.txt", Change.Action.EDIT, "1.2");
    }

    public void testChangesWithRemoval() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithRemoval";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(4, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.DELETE, "1.2");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt re-checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.3");

        changelist = changes.get(3);
        assertChangelistValues(changelist, "daniel", "file1.txt re-removed by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.DELETE, "1.4");
    }

    public void testChangesWithAdd() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithAdd";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(1, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt and dir/file3.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(1), "file2.txt", Change.Action.ADD, "1.1");
        assertChangeValues(changelist.getChanges().get(2), "dir/file3.txt", Change.Action.ADD, "1.1");
    }

    public void testChangesWithModify() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithModify";
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);

        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.2");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt modified by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.3");
    }

    public void testChangesWithBranch() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testChangesWithBranch";
        cvs.setRevision("BRANCH");
        List<Changelist> changes = cvs.getChangeLists(module);
        assertEquals(3, changes.size());
        assertValidChangeSets(changes);

        // we get the commit message from when the file1.txt was committed to head since the
        // branching process is just tagging, and does not represent a commit. The message is
        // retrieved as part of the version of the file, which is 1.1 until a commit on the branch
        // makes a change. Its a little odd, but thats cvs.
        Changelist changelist = changes.get(0);
        assertChangelistValues(changelist, "daniel", "file1.txt and file2.txt checked in by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.ADD, "1.1");

        changelist = changes.get(1);
        assertChangelistValues(changelist, "daniel", "file3.txt checked in on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file3.txt", Change.Action.ADD, "1.1.2.1");

        changelist = changes.get(2);
        assertChangelistValues(changelist, "daniel", "file1.txt modified on BRANCH by author a\n");
        assertChangeValues(changelist.getChanges().get(0), "file1.txt", Change.Action.EDIT, "1.1.2.1");
    }

    public void testCheckoutBranch() throws SCMException, IOException
    {
        String module = "unit-test/CvsClientTest/testCheckoutBranch";
        cvs.setRevision("BRANCH");
        cvs.setLocalPath(workdir);
        cvs.checkout(module);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
        assertTrue(new File(workdir, module + "/file3.txt").exists());

        // now checkout head and ensure that file3 is not there.
        removeDirectory(workdir);
        workdir = FileSystemUtils.createTempDirectory(CvsClientTest.class.getName(), "");

        cvs.setRevision(null);
        cvs.setLocalPath(workdir);
        cvs.checkout(module);

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertTrue(new File(workdir, module + "/file2.txt").exists());
        assertFalse(new File(workdir, module + "/file3.txt").exists());
    }

    public void testCheckoutDirectory() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testCheckoutDirectory";
        cvs.setLocalPath(workdir);
        cvs.checkout(module + "/dir");

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/dir/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
    }

    public void testCheckoutFile() throws SCMException
    {
        String module = "unit-test/CvsClientTest/testCheckoutFile";
        cvs.setLocalPath(workdir);
        cvs.checkout(module + "/file1.txt");

        // check that the selected files exist.
        assertTrue(new File(workdir, module + "/file1.txt").exists());
        assertFalse(new File(workdir, module + "/file2.txt").exists());
    }

    public void testCheckoutModule() throws SCMException
    {
        String module = "module";
        cvs.setLocalPath(workdir);
        cvs.checkout(module);

        assertTrue(new File(workdir, "unit-test/CvsClientTest/testCheckoutModule/dir1/file1.txt").exists());
        assertTrue(new File(workdir, "unit-test/CvsClientTest/testCheckoutModule/dir2/file2.txt").exists());
    }

    public void testHasBranchChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasBranchChangedSince";
        cvs.setRevision("BRANCH");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(cvs.hasChangedSince(dateFormat.parse("2006-03-10"), module));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2006-03-11"), module));
    }

    public void testHasDirectoryChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasDirectoryChangedSince";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(cvs.hasChangedSince(dateFormat.parse("2006-03-10"), module + "/dir"));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2006-03-11"), module + "/dir"));
    }

    public void testHasFileChangedSince() throws SCMException, ParseException
    {
        String module = "unit-test/CvsClientTest/testHasFileChangedSince";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(cvs.hasChangedSince(dateFormat.parse("2006-03-10"), module + "/file1.txt"));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2006-03-11"), module + "/file1.txt"));
    }

    public void testHasModuleChangedSince() throws SCMException
    {
        // module check does not work.
    }

    private static void assertChangelistValues(Changelist changelist, String user, String comment)
    {
        assertEquals(user, changelist.getUser());
        assertEquals(comment, changelist.getComment());
    }

    private static void assertChangeValues(Change change, String file, Change.Action action, String revision)
    {
        assertEndsWith(file, change.getFilename());
        assertEquals(action, change.getAction());
        assertEquals(revision, change.getRevision());
    }

    // assert that files are unique.
    private static void assertValidChangeSets(List<Changelist> changelists)
    {
        for (Changelist changelist : changelists)
        {
            assertValidChangeSet(changelist);
        }
    }

    private static void assertValidChangeSet(Changelist changelist)
    {
        List<Change> changes = changelist.getChanges();
        Map<String, String> filenames = new HashMap<String, String>();

        for (Change change : changes)
        {
            assertFalse(filenames.containsKey(change.getFilename()));
            filenames.put(change.getFilename(), change.getFilename());

            assertNotNull(change.getRevision());
            assertNotNull(change.getAction());
        }
    }
}
