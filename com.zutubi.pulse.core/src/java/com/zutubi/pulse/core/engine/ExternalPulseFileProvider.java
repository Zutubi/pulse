package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.InputStream;

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

    public String getFileContent(FileResolver resolver) throws Exception
    {
        InputStream is = resolver.resolve(path);
        return IOUtils.inputStreamToString(is);
    }

    public File getImportRoot()
    {
        return importRoot;
    }
}
