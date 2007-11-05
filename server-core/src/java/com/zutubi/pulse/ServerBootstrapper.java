package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildProperties;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void bootstrap(ExecutionContext context)
    {
        // ensure that the paths exist
        try
        {
            FileSystemUtils.createDirectory(new File(context.getString(BuildProperties.PROPERTY_OUTPUT_DIR)));
            FileSystemUtils.createDirectory(context.getWorkingDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
