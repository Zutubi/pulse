package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.scm.Change;
import com.cinnamonbob.scm.Changelist;
import com.cinnamonbob.model.SimpleChange;
import com.cinnamonbob.model.CvsRevision;
import com.cinnamonbob.util.FileSystemUtils;
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
public class CVSServerTest extends TestCase
{
    /**
     * Test cvs repository
     */
    //TODO: make the cvsRoot configurable.
    private CVSRoot cvsRoot = CVSRoot.parse(":local:/e/cvsroot");
    
    //TODO: make the workdir configurable.
    private File workdir = new File("E:/tmp/blah/blah/blah");
    
    public void setUp() throws Exception
    {
        // cleanup the working directory.
        Logger.setLogging("system");
        
        if (!FileSystemUtils.removeDirectory(workdir))
        {
            throw new RuntimeException("Failed to setup test case.");
        }
        workdir.mkdirs();
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
        CVSServer cvsServer = new CVSServer(cvsRoot);
        cvsServer.checkout(workdir, "project");
        
        assertTrue(foo.exists());
    }
    
    public void testGetChanges() throws Exception
    {
        // get changes since the start.
        CVSServer cvsServer = new CVSServer(cvsRoot);
        List<Changelist> changes = cvsServer.getChanges(workdir, null);
        assertNotNull(changes);
        assertTrue(changes.size() == 9);        
        assertEquals(2, changes.get(0).getChanges().size());
        assertEquals(1, changes.get(1).getChanges().size());
        assertEquals(2, changes.get(2).getChanges().size());
        assertEquals(1, changes.get(3).getChanges().size());
        assertEquals(1, changes.get(4).getChanges().size());
        assertEquals(2, changes.get(5).getChanges().size());
        assertEquals(1, changes.get(6).getChanges().size());
        assertEquals(1, changes.get(7).getChanges().size());
        assertEquals(2, changes.get(8).getChanges().size());
        assertValidChangeSets(changes);
        // get changes since x where x is the date of one of the changes.
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        changes = cvsServer.getChanges(workdir, dateFormat.parse("2005-05-10"));
        assertNotNull(changes);
        assertTrue(changes.size() == 4);
    }

    public void testGetChangesFromAtlassian() throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        CVSRoot root = CVSRoot.parse(":ext:dostermeier:4edueWX7@cvs.atlassian.com:/cvsroot/atlassian");
        CVSServer cvsServer = new CVSServer(root);
        List<Changelist> changes = cvsServer.getChanges(null, dateFormat.parse("2005-08-05"));
        System.out.println("changes:" + changes.size());
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
        String message = null;
        String author = null;
        
        for (Change change: changes)
        {
            SimpleChange simpleChange = (SimpleChange) change;
            assertFalse(filenames.containsKey(simpleChange.getFilename()));
            filenames.put(simpleChange.getFilename(), simpleChange.getFilename());

            // TODO fix for new revision types
            //CvsRevision revision = (CvsRevision)simpleChange.getRevision();
//            String currentComment = revision.getComment();
//            if (message == null)
//            {
//                message = currentComment;
//            }
//            assertEquals(message, currentComment);
//
//            String currentAuthor = revision.getAuthor();
//            if (author == null)
//            {
//                author = currentAuthor;
//            }
//            assertEquals(author, currentAuthor);
        }
    }
}
