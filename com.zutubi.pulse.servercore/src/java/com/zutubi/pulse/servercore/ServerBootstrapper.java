package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.util.FileSystemUtils;

import java.io.IOException;

/**
 * A simple bootstrapper implementation that ensures that the
 * necessary context directories exist.
 *
 * This bootstrapper should always be the first bootstrapper used. 
 */
public class ServerBootstrapper extends BootstrapperSupport
{
    public void doBootstrap(CommandContext commandContext)
    {
        if (!isTerminated())
        {
            try
            {
                ExecutionContext context = commandContext.getExecutionContext();
                FileSystemUtils.createDirectory(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR));
                FileSystemUtils.createDirectory(context.getWorkingDir());
            }
            catch (IOException e)
            {
                throw new BuildException(e);
            }
        }
    }
}
