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

package com.zutubi.pulse.core.marshal;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.PathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class LocalFileResolverTest extends PulseTestCase
{
    private static final String FILENAME = "f";
    private static final String DIRECTORY = "nested";
    private static final String CONTENT_SIMPLE = "content";
    private static final String CONTENT_NESTED = "nested content";

    private File tempDir;
    private LocalFileResolver resolver;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = createTempDirectory();
        resolver = new LocalFileResolver(tempDir);
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testSimple() throws Exception
    {
        File f = new File(tempDir, FILENAME);
        Files.write(CONTENT_SIMPLE, f, Charset.defaultCharset());
        final String resolvedContent = new ByteSource()
        {
            @Override
            public InputStream openStream() throws IOException
            {
                try
                {
                    return resolver.resolve(FILENAME);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }.asCharSource(Charset.defaultCharset()).read();

        assertEquals(CONTENT_SIMPLE, resolvedContent);
    }

    public void testNested() throws Exception
    {
        File dir = new File(tempDir, DIRECTORY);
        assertTrue(dir.mkdir());
        File nested = new File(dir, FILENAME);
        Files.write(CONTENT_NESTED, nested, Charset.defaultCharset());
        final String resolvedContent = new ByteSource()
        {
            @Override
            public InputStream openStream() throws IOException
            {
                try
                {
                    return resolver.resolve(PathUtils.getPath(DIRECTORY, FILENAME));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }.asCharSource(Charset.defaultCharset()).read();

        assertEquals(CONTENT_NESTED, resolvedContent);
    }
}
