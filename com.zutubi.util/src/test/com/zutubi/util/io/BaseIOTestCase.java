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

package com.zutubi.util.io;

import com.zutubi.util.RandomUtils;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.*;

public abstract class BaseIOTestCase extends ZutubiTestCase
{
    protected File tmp;

    protected void setUp() throws Exception
    {
        super.setUp();
        tmp = createTempDirectory();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        super.tearDown();
    }

    /**
     * Generate a new randomly named test file that contains the line number on each line.
     *
     * @param lineCount in lines for this file.
     *
     * @return a randomly named file
     *
     * @throws java.io.IOException is raised if we have problems writing to the file.
     */
    protected File generateTestFile(long lineCount) throws IOException
    {
        return generateTestFile(lineCount, 0, 0);
    }

    protected File generateTestFile(long lineCount, long lineLength, long offset) throws IOException
    {
        File file = createRandomFile();

        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            for (long l = 0; l < lineCount; l++)
            {
                out.write(getLine(l + offset, lineLength));
                out.newLine();
            }
        }
        finally
        {
            IOUtils.close(out);
        }
        return file;
    }

    protected String getLine(long number, long lineLength)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(Long.toString(number));
        while (builder.length() < lineLength)
        {
            builder.append('-');
        }

        return builder.toString();
    }

    protected File createRandomFile() throws IOException
    {
        String randomName = RandomUtils.insecureRandomString(10);
        File file = new File(tmp, randomName);
        while (file.exists())
        {
            randomName = RandomUtils.insecureRandomString(10);
            file = new File(tmp, randomName);
        }

        if (!file.createNewFile())
        {
            throw new IOException("Unexpected failure to create temporary file.");
        }
        return file;
    }
}
