package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.util.FileSystemUtils;

import java.io.IOException;

/**
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(ExecutionContext context) throws BuildException
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
