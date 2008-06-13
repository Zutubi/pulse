package com.zutubi.pulse.local;

import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.test.LinePreprocessor;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

/**
 */
public class PostProcessCommandTest extends PulseTestCase
{
    private File tmpDir;
    private boolean generateMode = false;
    private PostProcessCommand command = new PostProcessCommand();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tmpDir = FileSystemUtils.createTempDir(PostProcessCommandTest.class.getName(), "");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);
        super.tearDown();
    }

    private File getDataFile(String suffix)
    {
        File root = getPulseRoot();
        return new File(root, FileSystemUtils.composeFilename("local", "src", "test", "com", "zutubi", "pulse", "local", getClass().getSimpleName() + "." + suffix));
    }

    private File getPulseFile()
    {
        return getDataFile("pulse.xml");
    }

    private File getInput()
    {
        return getDataFile("in.txt");
    }

    private File getExpectedOutput(String name)
    {
        return getDataFile(name + ".out.txt");
    }

    public void testTests() throws Exception
    {
        simpleCase("test");
    }

    public void testFeatures() throws Exception
    {
        simpleCase("compile");
    }

    private void simpleCase(String name) throws IOException, PulseException, URISyntaxException
    {
        File out = new File(tmpDir,"output.txt");
        PrintStream stream = null;
        try
        {
            stream = new PrintStream(out);
            String[] argv = new String[]{"-p", getPulseFile().getAbsolutePath(), name, getInput().getAbsolutePath()};
            command.execute(argv, stream, stream);
        }
        finally
        {
            IOUtils.close(stream);
        }

        compareOutput(name, out);
    }

    private void compareOutput(String name, File output) throws IOException
    {
        File expectedOutput = getExpectedOutput(name);

        if (generateMode)
        {
            output.renameTo(expectedOutput);
        }
        else
        {
            assertFilesEqual(expectedOutput, output, new LinePreprocessor()
            {
                public String processLine(String line)
                {
                    return line.replaceAll("((?:pulse|input) file *:).*", "$1");
                }
            });
        }
    }
}
