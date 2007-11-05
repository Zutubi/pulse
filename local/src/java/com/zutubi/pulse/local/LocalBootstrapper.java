package com.zutubi.pulse.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildProperties;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(ExecutionContext context) throws BuildException
    {
        try
        {
            FileSystemUtils.cleanOutputDir(context.getInternalFile(BuildProperties.PROPERTY_OUTPUT_DIR));
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
