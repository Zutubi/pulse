package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;

import java.util.Set;

/**
 */
public class ScmClientUtils
{
    public static Set<ScmCapability> getCapabilities(ScmConfiguration config, ScmClientFactory clientFactory) throws ScmException
    {
        ScmClient client = null;
        try
        {
            client = clientFactory.createClient(config);
            return client.getCapabilities();
        }
        finally
        {
            close(client);
        }
    }

    public static void close(ScmClient client)
    {
        if (client != null)
        {
            client.close();
        }
    }
}
