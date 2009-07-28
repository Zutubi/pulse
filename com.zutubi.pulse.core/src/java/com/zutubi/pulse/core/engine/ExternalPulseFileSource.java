package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.util.io.IOUtils;

import java.io.InputStream;

/**
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
