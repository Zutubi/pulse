package com.cinnamonbob.local;

import com.cinnamonbob.core.BobException;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.test.BobTestCase;

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
        // Create a temporary working directory
        tmpDir = FileSystemUtils.createTempDirectory(LocalBuildTest.class.getName(), "");
        builder = new LocalBuild();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.removeDirectory(tmpDir);
    }

    private File getExpectedOutput(String name)
    {
        URL url = getClass().getResource(getClass().getSimpleName() + ".basic.xml");
        File xmlFile = new File(url.getFile());
        File dataDir = new File(xmlFile.getParentFile(), "data");

        return new File(dataDir, name);
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
        String bobFile = copyFile("basic");

        builder.runBuild(tmpDir, bobFile, "my-default", null, "out");
        compareOutput("basic");
    }

    public void testInvalidWorkDir() throws BobException
    {
        File workDir = new File("/no/such/dir");
        try
        {
            builder.runBuild(workDir, "bob.xml", "my-default", null, "out");
        }
        catch (BobException e)
        {
            assertEquals("Working directory '" + workDir.getAbsolutePath() + "' does not exist", e.getMessage());
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

        if (generateMode)
        {
            tmpDir.renameTo(new File(expectedDir.getAbsolutePath().replace("classes", "src/test")));
        }
        else
        {
            assertDirectoriesEqual(expectedDir, tmpDir);
        }
    }
}
