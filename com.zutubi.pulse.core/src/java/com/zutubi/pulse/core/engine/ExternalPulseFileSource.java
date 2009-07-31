package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.util.io.IOUtils;

import java.io.InputStream;

/**
 * Sources pulse files from an external source by looking them up via a
 * {@link com.zutubi.pulse.core.marshal.FileResolver}.  The classic use-case for this is versioned
 * projects, where the pulse file is in the project's source code.
 */
public class ExternalPulseFileSource implements PulseFileSource
{
    private String path;

    public ExternalPulseFileSource(String path)
    {
        this.path = path;
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
}
