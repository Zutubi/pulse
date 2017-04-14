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

package com.zutubi.pulse.core.engine;

import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.zutubi.pulse.core.marshal.FileResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Provides pulse files from an external source by looking them up via a
 * {@link com.zutubi.pulse.core.marshal.FileResolver}.  The classic use-case for this is versioned
 * projects, where the pulse file is in the project's source code.
 */
public class ExternalPulseFileProvider implements PulseFileProvider
{
    private String path;
    private File importRoot;

    public ExternalPulseFileProvider(String path)
    {
        this(path, null);
    }

    public ExternalPulseFileProvider(String path, File importRoot)
    {
        this.path = path;
        this.importRoot = importRoot;
    }

    public String getPath()
    {
        return path;
    }

    public String getFileContent(final FileResolver resolver) throws Exception
    {
        return new ByteSource()
        {
            @Override
            public InputStream openStream() throws IOException
            {
                try
                {
                    return resolver.resolve(path);
                }
                catch (Exception e)
                {
                    throw new IOException(e);
                }
            }
        }.asCharSource(Charset.defaultCharset()).read();
    }

    public File getImportRoot()
    {
        return importRoot;
    }
}
