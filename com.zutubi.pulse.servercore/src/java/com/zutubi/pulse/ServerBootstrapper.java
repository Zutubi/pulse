package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.util.FileSystemUtils;

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
            FileSystemUtils.createDirectory(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR));
            FileSystemUtils.createDirectory(context.getWorkingDir());
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
