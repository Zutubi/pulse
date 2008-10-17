package com.zutubi.pulse.core.scm.p4.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.PerforceClient;
import com.zutubi.pulse.core.scm.ScmClientUtils;

/**
 */
@SymbolicName("zutubi.perforceConfigurationCheckHandler")
public class PerforceConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<PerforceConfiguration>
{
    private ScmClientFactory scmClientFactory;

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
            ScmClientUtils.close(client);
        }
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
