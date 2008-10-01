package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 */
public class LocalBuildTest extends PulseTestCase
{
    File tmpDir;
    boolean generateMode = false;
    static LocalBuild builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Create a temporary base directory
        tmpDir = FileSystemUtils.createTempDir(LocalBuildTest.class.getName(), "");
        if (builder == null)
        {
            builder = LocalBuild.bootstrap();
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        SpringComponentContext.closeAll();
        removeDirectory(tmpDir);
        super.tearDown();
    }

    private File getExpectedOutput(String name)
    {
        File root = getPulseRoot();
        return new File(root, FileSystemUtils.composeFilename("com.zutubi.pulse.dev", "src", "test", "com", "zutubi", "pulse", "dev", "local", "data", name));
    }

    private String copyFile(String name) throws IOException, URISyntaxException
    {
        URL pulseURL = getInputURL(name);
        File srcFile = new File(pulseURL.toURI());
        File destFile = new File(tmpDir, srcFile.getName());

        IOUtils.copyFile(srcFile, destFile);
        return srcFile.getName();
    }

    public void testBasicBuild() throws Exception
    {
        simpleCase("basic");
    }

    public void testInvalidBaseDir() throws PulseException
    {
        File baseDir = new File("/no/such/dir");
        try
        {
            builder.runBuild(baseDir, "pulse.xml", "my-default", null, "out");
        }
        catch (PulseException e)
        {
            assertEquals("Base directory '" + baseDir.getAbsolutePath() + "' does not exist", e.getMessage());
            return;
        }

        assertTrue("Expected exception", false);
    }

    public void testInvalidPulseFile() throws PulseException, IOException
    {
        try
        {
            builder.runBuild(tmpDir, "no-such-pulse.xml", "my-default", null, "out");
            fail();
        }
        catch (PulseException e)
        {
            assertTrue(e.getMessage().contains("Unable to load pulse file"));
        }
    }

    public void testLoadResources() throws IOException, PulseException, URISyntaxException
    {
        String pulseFile = copyFile("resourceload");
        String resourceFile = new File(getInputURL("resources").toURI()).getAbsolutePath();

        builder.runBuild(tmpDir, pulseFile, null, resourceFile, "out");
        compareOutput("resourceload");
    }

    public void testDefaultResourceVersion() throws IOException, PulseException, URISyntaxException
    {
        String pulseFile = copyFile("defaultresource");
        String resourceFile = new File(getInputURL("resources").toURI()).getAbsolutePath();

        builder.runBuild(tmpDir, pulseFile, null, resourceFile, "out");
        compareOutput("defaultresource");
    }

    public void testCommandFailure() throws Exception
    {
        simpleCase("commandFailure");
    }

    public void testTests() throws IOException, PulseException, URISyntaxException
    {
        processTestsHelper("tests");
    }

    public void testTestFailures() throws IOException, PulseException, URISyntaxException
    {
        processTestsHelper("testfailures");
    }

    private void processTestsHelper(String name) throws IOException, URISyntaxException, PulseException
    {
        String pulseFile = copyFile("tests");
        File testFile = new File(getInputURL(name, "txt").toURI());
        File toFile = new File(tmpDir, "test-report.txt");
        IOUtils.copyFile(testFile, toFile);

        builder.runBuild(tmpDir, pulseFile, null, null, "out");
        compareOutput(name);
    }

    private void simpleCase(String name) throws IOException, PulseException, URISyntaxException
    {
        String pulseFile = copyFile(name);
        builder.runBuild(tmpDir, pulseFile, "my-default", null, "out");
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
        cleanEnvTxtFiles(tmpDir);

        if (generateMode)
        {
            removeDirectory(expectedDir);
            tmpDir.renameTo(expectedDir);
        }
        else
        {
            assertDirectoriesEqual(expectedDir, tmpDir);
        }
    }

    private void cleanEnvTxtFiles(File dir) throws IOException
    {
        // keep the first 6 lines of the file, delete the rest.
        File env = new File(dir, "env.txt");
        if (env.isFile())
        {
            File cleaned = new File(dir, "env.txt.cleaned");
            StringBuffer cleanedContent = new StringBuffer();
            BufferedReader reader = new BufferedReader(new StringReader(IOUtils.fileToString(env)));
            for (int i = 0; i < 6; i++)
            {
                cleanedContent.append(reader.readLine());
                cleanedContent.append('\n');
            }

            FileOutputStream output = null;
            try
            {
                output = new FileOutputStream(cleaned);
                output.write(cleanedContent.toString().getBytes());
            }
            finally
            {
                IOUtils.close(output);
            }

            env.delete();
        }

        for (String filename : dir.list())
        {
            File f = new File(dir, filename);
            if (f.isDirectory())
            {
                cleanEnvTxtFiles(f);
            }
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
