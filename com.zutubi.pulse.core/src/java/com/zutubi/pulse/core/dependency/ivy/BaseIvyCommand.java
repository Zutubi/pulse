package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_MASTER_URL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_SECURITY_HASH;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.ivy.util.url.CredentialsStore;

public abstract class BaseIvyCommand implements Command
{
    protected void updateIvyCredentials(ExecutionContext context)
    {
        try
        {
            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            CredentialsStore.INSTANCE.addCredentials("Pulse", host, "pulse", context.getString(NAMESPACE_INTERNAL, PROPERTY_SECURITY_HASH));
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
