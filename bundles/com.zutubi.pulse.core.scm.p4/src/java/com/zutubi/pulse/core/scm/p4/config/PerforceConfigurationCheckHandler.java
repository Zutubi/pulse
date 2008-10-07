package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceClient;

/**
 */
@SymbolicName("zutubi.perforceConfigurationCheckHandler")
public class PerforceConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<PerforceConfiguration>
{
    private ScmClientFactory scmClientFactory;

    public void test(PerforceConfiguration configuration) throws ScmException
    {
        PerforceClient client = (PerforceClient) scmClientFactory.createClient(configuration);
        try
        {
            client.testConnection();
        }
        finally
        {
            client.close();
        }
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
