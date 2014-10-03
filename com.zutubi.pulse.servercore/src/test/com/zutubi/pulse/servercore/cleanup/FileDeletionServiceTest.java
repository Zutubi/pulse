package com.zutubi.pulse.servercore.cleanup;

import com.google.common.io.Files;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.UserPaths;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FileDeletionServiceTest extends PulseTestCase
{
    private File tmp;
    private ConfigurationManager configurationManager;
    private FileDeletionService deletionService;
    private static final int DELETE_TIMEOUT = 10000;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir(getName(), "test");


        UserPaths paths = new UserPaths()
        {
            public File getData()
            {
                return tmp;
            }

            public File getUserConfigRoot()
            {
                return null;
            }
        };

        configurationManager = mock(ConfigurationManager.class);
        doReturn(paths).when(configurationManager).getUserPaths();

        initService();
    }

    private void initService()
    {
        deletionService = new FileDeletionService();
        deletionService.setThreadFactory(Executors.defaultThreadFactory());
        deletionService.setConfigurationManager(configurationManager);
        deletionService.init();
    }

    protected void tearDown() throws Exception
    {
        deletionService.stop(true);

        removeDirectory(tmp);

        super.tearDown();
    }

    public void testDeleteFile() throws IOException, ExecutionException, InterruptedException
    {
        File file = createNewFile("file.txt");

        assertTrue(deletionService.delete(file, false, false).get());

        assertFalse(file.exists());
    }

    public void testDeleteEmptyDirectory() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");

        assertTrue(deletionService.delete(dir, false, false).get());

        assertFalse(dir.exists());
    }

    public void testDeleteRenameClash() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");
        File file = createNewFile("dir.dead");

        assertTrue(deletionService.delete(dir, false, false).get());
        assertFalse(dir.exists());
        assertTrue(file.exists());
    }

    public void testRecursiveDeletion() throws IOException, ExecutionException, InterruptedException
    {
        File dir = createNewDir("dir");
        File nested = createNewDir(dir, "nested");
        File file = createNewFile(dir, "file.txt");

        assertTrue(deletionService.delete(dir, false, false).get());
        assertFalse(dir.exists());
        assertFalse(nested.exists());
        assertFalse(file.exists());
    }

    public void testNonExistantFile() throws ExecutionException, InterruptedException
    {
        assertTrue(deletionService.delete(new File("does not exist"), false, false).get());
    }

    public void testNullFile()
    {
        try
        {
            deletionService.delete(null, false, false);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
    
    public void testContinuesDeletionAfterRestart() throws IOException
    {
        deletionService.stop(true);
        
        File f1 = createNewFile("1");
        File f2 = createNewFile("2");
        
        deletionService.delete(f1, false, false);
        deletionService.delete(f2, false, false);
        
        assertFalse(f1.exists());
        assertFalse(f2.exists());

        File dead1 = new File(f1.getAbsolutePath() + FileDeletionService.SUFFIX);
        File dead2 = new File(f2.getAbsolutePath() + FileDeletionService.SUFFIX);
        assertTrue(dead1.exists());
        assertTrue(dead2.exists());
        
        initService();
        
        waitForFileToBeDeleted(dead1);
        waitForFileToBeDeleted(dead2);
    }

    public void testIndexEntriesRemoved() throws IOException, ExecutionException, InterruptedException
    {
        // When a file is fully deleted, it should be removed from the index.
        // We can test this by replacing it with a .dead file and ensuring it
        // is not nuked on restart.
        File f1 = createNewFile("1");

        Future<Boolean> future = deletionService.delete(f1, false, false);
        assertTrue(future.get());
        deletionService.stop(true);
        
        assertFalse(f1.exists());

        File dead1 = new File(f1.getAbsolutePath() + FileDeletionService.SUFFIX);
        assertFalse(dead1.exists());
        assertTrue(dead1.createNewFile());
        
        initService();

        Thread.sleep(100);
        assertTrue(dead1.exists());
    }

    public void testIgnoresInvalidIndexEntries() throws IOException, InterruptedException
    {
        deletionService.stop(true);
        
        File notDead = createNewFile("alive.txt");
        File index = new File(tmp, FileDeletionService.INDEX_FILE_NAME);
        Files.write("invalid\n" + notDead.getAbsolutePath() + "\n", index, Charset.defaultCharset());

        initService();
        
        Thread.sleep(100);
        assertTrue(notDead.exists());
    }
    
    public void testOutsideOfDataDisallowed() throws IOException, ExecutionException, InterruptedException
    {
        File dir = FileSystemUtils.createTempDir(getName(), null);
        try
        {
            Future<Boolean> future = deletionService.delete(dir, false, false);
            assertEquals(Boolean.TRUE, future.get());
            assertTrue(dir.isDirectory());
        }
        finally
        {
            removeDirectory(dir);
        }
    }
    
    public void testOutsideOfDataAllowed() throws IOException, ExecutionException, InterruptedException
    {
        File dir = FileSystemUtils.createTempDir(getName(), null);
        try
        {
            Future<Boolean> future = deletionService.delete(dir, false, true);
            assertEquals(Boolean.TRUE, future.get());
            waitForFileToBeDeleted(dir);
        }
        finally
        {
            removeDirectory(dir);
        }
    }

    private void waitForFileToBeDeleted(File file)
    {
        long start = System.currentTimeMillis();
        while (file.exists())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Keep going.
            }
            
            if (System.currentTimeMillis() - start > DELETE_TIMEOUT)
            {
                fail("Timed out waiting for file '" + file.getAbsolutePath() + "' to be deleted");
            }
        }
    }

    private File createNewDir(String dirname)
    {
        return createNewDir(tmp, dirname);
    }

    private File createNewDir(File base, String dirname)
    {
        File d = new File(base, dirname);
        assertTrue(d.mkdirs());
        return d;
    }

    private File createNewFile(String filename) throws IOException
    {
        return createNewFile(tmp, filename);
    }

    private File createNewFile(File base, String filename) throws IOException
    {
        File f = new File(base, filename);
        assertTrue(f.createNewFile());
        return f;
    }
}
