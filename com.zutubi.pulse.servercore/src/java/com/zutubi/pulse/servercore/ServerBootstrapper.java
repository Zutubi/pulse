package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void bootstrap(PulseExecutionContext context, CommandResult result)
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
