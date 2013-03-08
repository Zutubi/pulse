package com.zutubi.pulse.core.engine;

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
        final InputStream stream = resolver.resolve(path);
        return CharStreams.toString(CharStreams.newReaderSupplier(new InputSupplier<InputStream>()
        {
            public InputStream getInput() throws IOException
            {
                return stream;
            }
        }, Charset.defaultCharset()));
    }

    public File getImportRoot()
    {
        return importRoot;
    }
}
