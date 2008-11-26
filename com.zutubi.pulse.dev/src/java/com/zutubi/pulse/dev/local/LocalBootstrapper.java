package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(PulseExecutionContext context) throws BuildException
    {
        try
        {
            FileSystemUtils.cleanOutputDir(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR));
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
    }
}
