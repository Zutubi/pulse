package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsClient;
import com.zutubi.pulse.core.scm.ScmClientUtils;

/**
 *
 *
 */
@SymbolicName("zutubi.cvsConfigurationCheckHandler")
@Wire
public class CvsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<CvsConfiguration>
{
    private ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientFactory;

    public void test(CvsConfiguration configuration) throws ScmException
    {
        CvsClient client = null;
        try
        {
            client = (CvsClient) scmClientFactory.createClient(configuration);
            client.testConnection();
        }
        finally
        {
            ScmClientUtils.close(client);
        }
    }

    public void setScmClientFactory(ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}
