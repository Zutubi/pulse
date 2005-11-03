package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.scm.SCMException;
import junit.framework.TestCase;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.util.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class CvsClientTest extends TestCase
{
    /**
     * Test cvs repository
     */
    //TODO: make the cvsRoot configurable.
    private CVSRoot cvsRoot = CVSRoot.parse(":local:/e/cvsroot");
    private CvsClient cvs = new CvsClient(cvsRoot);

    private File workdir = null;
    
    public void setUp() throws Exception
    {
        super.setUp();

        Logger.setLogging("system");
        
        // cleanup the working directory.
        workdir = FileSystemUtils.createTempDirectory("CvsClient", "Test");
    }

    public void tearDown() throws Exception
    {
        if (!FileSystemUtils.removeDirectory(workdir))
        {
            throw new RuntimeException("Failed to setup test case.");
        }
        super.tearDown();
    }

    //NOTE: when using the 'local' method, ensure that the 'cvs' command
    //      is located in your path.
    
    /**
     * 
     */ 
    public void testCheckout() throws Exception
    {        
        // destination directory...        
        File foo = new File(workdir, "project/test/foo");        
        assertTrue(!foo.exists());
               
        // checkout...
        cvs.setLocalPath(workdir);
        cvs.checkout("project", null);
        
        assertTrue(foo.exists());
        assertFalse(new File(workdir, "project/test/branch.only").exists());
    }

    public void testCheckoutBranch() throws Exception
    {
        // checkout...
        cvs.setLocalPath(workdir);
        cvs.setBranch("BRANCH");
        cvs.checkout("project", null);

        assertTrue(new File(workdir, "project/test/branch.only").exists());
    }

    public void testHasChangedSince() throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(cvs.hasChangedSince(dateFormat.parse("2005-05-10"), null));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2005-10-10"), null));
    }

    public void testHasChangedSinceWithModule() throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertTrue(cvs.hasChangedSince(dateFormat.parse("2005-05-10"), "project"));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2005-05-10"), "project/test/foobar"));
        assertFalse(cvs.hasChangedSince(dateFormat.parse("2005-10-10"), "project"));
    }

    public void testBugCheckoutHangs() throws Exception
    {
        // checkout...
        cvs.setLocalPath(workdir);
        // attempting to checkout with a leading '/' results in the CheckoutCommand hanging.
        try
        {
            cvs.checkout("/e/cvsroot/project", null);
            assertFalse(true);
        } catch (SCMException e)
        {
            // noop.
        }
    }

    public void testGetChanges() throws Exception
    {
        // get changes since the start.
        List<Changelist> changes = cvs.getChangeLists(null, null);
        assertNotNull(changes);
        assertTrue(changes.size() == 10);
        assertEquals(2, changes.get(0).getChanges().size());
        assertEquals(1, changes.get(1).getChanges().size());
        assertEquals(2, changes.get(2).getChanges().size());
        assertEquals(1, changes.get(3).getChanges().size());
        assertEquals(1, changes.get(4).getChanges().size());
        assertEquals(2, changes.get(5).getChanges().size());
        assertEquals(1, changes.get(6).getChanges().size());
        assertEquals(1, changes.get(7).getChanges().size());
        assertEquals(2, changes.get(8).getChanges().size());
        assertEquals(1, changes.get(9).getChanges().size());
        assertValidChangeSets(changes);
        // get changes since x where x is the date of one of the changes.
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        changes = cvs.getChangeLists(dateFormat.parse("2005-05-10"), null);
        assertNotNull(changes);
        assertTrue(changes.size() == 5);
    }

    public void testGetChangesFromAtlassian() throws Exception
    {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        CVSRoot root = CVSRoot.parse(":ext:dostermeier:4edueWX7@cvs.atlassian.com:/cvsroot/atlassian");
//        CvsClient cvs = new CvsClient(root);
//        List<Changelist> changes = cvs.getChangeLists(dateFormat.parse("2005-10-05"), null);
//        System.out.println("changes:" + changes.size());
    }
    
    public void testUpdate() throws Exception
    {
        // check that the contents of a particular file are as 
        // expected after the update.
    }
    
    // assert that files are unique.
    private void assertValidChangeSets(List<Changelist> changelists)
    {
        for (Changelist changelist: changelists)
        {
            assertValidChangeSet(changelist);
        }
    }
    
    private void assertValidChangeSet(Changelist changelist)
    {
        List<Change> changes = changelist.getChanges();
        Map<String, String> filenames = new HashMap<String, String>();

        for (Change change: changes)
        {
            assertFalse(filenames.containsKey(change.getFilename()));
            filenames.put(change.getFilename(), change.getFilename());

            assertNotNull(change.getRevision());
            assertNotNull(change.getAction());
        }
    }
}
