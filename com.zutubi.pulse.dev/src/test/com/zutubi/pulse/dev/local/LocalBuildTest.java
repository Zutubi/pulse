package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.IOAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class LocalBuildTest extends PulseTestCase
{
    private File tmpDir;
    private File baseDir;
    private File expectedDir;
    private boolean generateMode = false;
    private static LocalBuild builder;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Create a temporary base directory
        tmpDir = FileSystemUtils.createTempDir(LocalBuildTest.class.getName(), "");
        baseDir = new File(tmpDir, "base");
        assertTrue(baseDir.mkdir());
        expectedDir = new File(tmpDir, "expected");
        assertTrue(expectedDir.mkdir());

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

    private String copyFile(String name) throws IOException, URISyntaxException
    {
        URL pulseURL = getInputURL(name, "xml");
        File srcFile = new File(pulseURL.toURI());
        File destFile = new File(baseDir, srcFile.getName());

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
            builder.runBuild(baseDir, "no-such-pulse.xml", "my-default", null, "out");
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
        String resourceFile = new File(getInputURL("resources", "xml").toURI()).getAbsolutePath();

        builder.runBuild(baseDir, pulseFile, null, resourceFile, "out");
        compareOutput("resourceload");
    }

    public void testDefaultResourceVersion() throws IOException, PulseException, URISyntaxException
    {
        String pulseFile = copyFile("defaultresource");
        String resourceFile = new File(getInputURL("resources", "xml").toURI()).getAbsolutePath();

        builder.runBuild(baseDir, pulseFile, null, resourceFile, "out");
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
        File toFile = new File(baseDir, "test-report.txt");
        IOUtils.copyFile(testFile, toFile);

        builder.runBuild(baseDir, pulseFile, null, null, "out");
        compareOutput(name);
    }

    private void simpleCase(String name) throws IOException, PulseException, URISyntaxException
    {
        String pulseFile = copyFile(name);
        builder.runBuild(baseDir, pulseFile, "my-default", null, "out");
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
                line = line.replace(baseDir.getAbsolutePath(), "tmpDir");
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

        assertTrue(log.delete());
    }

    private void compareOutput(String expectedName) throws IOException
    {
        cleanBuildLog(new File(baseDir, "build.log"));
        cleanExceptionFiles(baseDir);
        cleanEnvTxtFiles(baseDir);

        if (generateMode)
        {
            File zipFile = new File(getClass().getSimpleName() + "." + expectedName + ".zip");
            ZipUtils.createZip(zipFile, baseDir, null);
            System.out.println("Expected output archive generated at '" + zipFile.getAbsolutePath() + "'");
        }
        else
        {
            unzipInput(expectedName, expectedDir);
            IOAssertions.assertDirectoriesEqual(expectedDir, baseDir);
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

            assertTrue(env.delete());
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
