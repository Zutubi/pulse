package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_MASTER_URL;
import org.apache.ivy.util.url.CredentialsStore;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The base for commands that specifically require ivy.    
 */
public abstract class BaseIvyCommand implements Command
{
    protected void updateIvyCredentials(PulseExecutionContext context)
    {
        try
        {
            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            CredentialsStore.INSTANCE.addCredentials("Pulse", host, "pulse", context.getSecurityHash());
        }
        catch (MalformedURLException e)
        {
            throw new BuildException(e);
        }
    }

    public void terminate()
    {
        // noop.
    }
}
