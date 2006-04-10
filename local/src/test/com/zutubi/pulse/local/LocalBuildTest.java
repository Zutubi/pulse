package com.zutubi.pulse.local;

import com.zutubi.pulse.core.BobException;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.test.BobTestCase;

import java.io.*;
import java.net.URL;

/**
 */
public class LocalBuildTest extends BobTestCase
{
    File tmpDir;
    boolean generateMode = false;
    LocalBuild builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Create a temporary base directory
        tmpDir = FileSystemUtils.createTempDirectory(LocalBuildTest.class.getName(), "");
        builder = new LocalBuild();
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    private File getExpectedOutput(String name)
    {
        File root = getBobRoot();
        return new File(root, FileSystemUtils.composeFilename("local", "src", "test", "com", "zutubi", "pulse", "local", "data", name));
    }

    private String copyFile(String name) throws IOException
    {
        URL bobURL = getInputURL(name);
        File srcFile = new File(bobURL.getFile());
        File destFile = new File(tmpDir, srcFile.getName());

        IOUtils.copyFile(srcFile, destFile);
        return srcFile.getName();
    }

    public void testBasicBuild() throws Exception
    {
        simpleCase("basic");
    }

    public void testInvalidBaseDir() throws BobException
    {
        File baseDir = new File("/no/such/dir");
        try
        {
            builder.runBuild(baseDir, "bob.xml", "my-default", null, "out");
        }
        catch (BobException e)
        {
            assertEquals("Base directory '" + baseDir.getAbsolutePath() + "' does not exist", e.getMessage());
            return;
        }

        assertTrue("Expected exception", false);
    }

    public void testInvalidBobFile() throws BobException, IOException
    {
        try
        {
            builder.runBuild(tmpDir, "no-such-bob.xml", "my-default", null, "out");
            fail();
        }
        catch (BobException e)
        {
            assertTrue(e.getMessage().contains("Unable to load bob file"));
        }
    }

    public void testLoadResources() throws IOException, BobException
    {
        String bobFile = copyFile("resourceload");
        String resourceFile = getInputURL("resources").getFile();

        builder.runBuild(tmpDir, bobFile, null, resourceFile, "out");
        compareOutput("resourceload");
    }

    public void testCommandFailure() throws Exception
    {
        simpleCase("commandFailure");
    }

    private void simpleCase(String name) throws IOException, BobException
    {
        String bobFile = copyFile(name);
        builder.runBuild(tmpDir, bobFile, "my-default", null, "out");
        compareOutput(name);
    }

    private void cleanBuildLog(File log) throws IOException
    {
        File output = new File(log.getAbsolutePath() + ".cleaned");

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(log));
            writer = new BufferedWriter(new FileWriter(output));
            String line;

            while ((line = reader.readLine()) != null)
            {
                line = line.replaceFirst("commenced:.*", "commenced:");
                line = line.replaceFirst("completed:.*", "completed:");
                line = line.replaceFirst("elapsed  :.*", "elapsed  :");
                line = line.replaceFirst("The system cannot find the file specified", "No such file or directory");
                line = line.replace(tmpDir.getAbsolutePath(), "tmpDir");
                line = line.replaceAll("\\\\", "/");
                writer.write(line);
                writer.newLine();
            }
        }
        finally
        {
            IOUtils.close(reader);
            IOUtils.close(writer);
        }

        log.delete();
    }

    private void compareOutput(String expectedName) throws IOException
    {
        File expectedDir = getExpectedOutput(expectedName);
        cleanBuildLog(new File(tmpDir, "build.log"));

        cleanExceptionFiles(tmpDir);

        if (generateMode)
        {
            tmpDir.renameTo(expectedDir);
        }
        else
        {
            assertDirectoriesEqual(expectedDir, tmpDir);
        }
    }

    private void cleanExceptionFiles(File dir) throws IOException
    {
        File exceptionFile = new File(dir, "exception");
        if (exceptionFile.exists() && !exceptionFile.delete())
        {
            throw new IOException("Failed to delete the file '" + exceptionFile + "'.");
        }

        for (String filename : dir.list())
        {
            File f = new File(dir, filename);
            if (f.isDirectory())
            {
                cleanExceptionFiles(f);
            }
        }

    }
}
