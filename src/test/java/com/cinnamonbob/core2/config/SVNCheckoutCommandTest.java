package com.cinnamonbob.core2.config;

import com.cinnamonbob.util.FileSystemUtils;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public class SVNCheckoutCommandTest extends TestCase
{
    private File outputDir;
    private File workDir;
    
    public void setUp() throws Exception
    {
        outputDir = FileSystemUtils.createTmpDirectory(SVNCheckoutCommandTest.class.getName(), "output");    
        workDir = FileSystemUtils.createTmpDirectory(SVNCheckoutCommandTest.class.getName(), "work");    
    }
    
    public void tearDown() throws Exception
    {
        assertTrue(FileSystemUtils.removeDirectory(outputDir));
        assertTrue(FileSystemUtils.removeDirectory(workDir));
    }
    
    //NOTE: This is not a very good unit test, most of the functionality is 
    //NOTE: is not related to the checkout command itself, but to the SVN server.
    
    public void testCheckout()
    {
        //TODO: checkout a test datadirectory with known content.
        
        SVNCheckoutCommand svn = new SVNCheckoutCommand();
        svn.setUser("daniel");
        svn.setPassword("4edueWX7");
        svn.setPath(workDir);
        svn.setUrl("svn+ssh://cinnamonbob.com/usr/local/svn-repo/bob/trunk/src/bin");        
        
        CommandResult result = svn.execute(outputDir);
        assertTrue(result.succeeded());
        
        File[] files = workDir.listFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);
        List<Artifact> artifacts = result.getArtifacts();
        assertNotNull(artifacts.get(0));
    }
}
