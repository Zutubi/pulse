package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.plugins.BasePluginSystemTestCase;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.IOAssertions;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.UnaryFunction;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

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
        BasePluginSystemTestCase.OSGIUtilsAccessor.reset();
        SpringComponentContext.closeAll();
        removeDirectory(tmpDir);
        super.tearDown();
    }

    private File getPulseFile()
    {
        return getInputFile("pulse", "xml");
    }

    private File getInput()
    {
        return getInputFile("in", "txt");
    }

    private File getExpectedOutput(String name)
    {
        return getInputFile(name + ".out" , "txt");
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
            assertTrue(output.renameTo(expectedOutput));
        }
        else
        {
            IOAssertions.assertFilesEqual(expectedOutput, output, new UnaryFunction<String, String>()
            {
                public String process(String line)
                {
                    if (line == null)
                    {
                        return null;
                    }
                    else
                    {
                        return line.replaceAll("((?:pulse|input) file *:).*", "$1");
                    }
                }
            });
        }
    }
}
