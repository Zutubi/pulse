/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.local;

import com.google.common.base.Function;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.junit.IOAssertions;

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
            IOAssertions.assertFilesEqual(expectedOutput, output, new Function<String, String>()
            {
                public String apply(String line)
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
