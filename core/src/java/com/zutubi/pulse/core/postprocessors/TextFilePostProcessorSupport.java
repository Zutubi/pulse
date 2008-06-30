package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 */
public abstract class TextFilePostProcessorSupport extends PostProcessorSupport
{
    protected void process(File artifactFile, PostProcessorContext ppContext)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(artifactFile));
            process(reader, ppContext);
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    protected abstract void process(BufferedReader reader, PostProcessorContext ppContext) throws IOException;
}
