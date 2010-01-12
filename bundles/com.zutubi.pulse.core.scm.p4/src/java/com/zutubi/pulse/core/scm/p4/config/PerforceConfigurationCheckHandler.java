package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

/**
 * Tests the connection to a Perforce server.
 */
@Wire
@SymbolicName("zutubi.perforceConfigurationCheckHandler")
public class PerforceConfigurationCheckHandler extends AbstractConfigurationCheckHandler<PerforceConfiguration>
{
    private ScmClientFactory<? super PerforceConfiguration> scmClientFactory;

    public void test(PerforceConfiguration configuration) throws ScmException
    {
        PerforceClient client = null;
        try
        {
            client = (PerforceClient) scmClientFactory.createClient(configuration);
            client.testConnection();
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }
    }

    public void setScmClientFactory(ScmClientFactory<? super PerforceConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
