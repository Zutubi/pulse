package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsClient;

/**
 *
 *
 */
@SymbolicName("zutubi.cvsConfigurationCheckHandler")
public class CvsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<CvsConfiguration>
{
    private ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientFactory;

    public void test(CvsConfiguration configuration) throws ScmException
    {
        CvsClient client = (CvsClient) scmClientFactory.createClient(configuration);
        try
        {
            client.testConnection();
        }
        finally
        {
            client.close();
        }
    }

    public void setScmClientFactory(ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}
