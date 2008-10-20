package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.pulse.core.scm.ScmClientUtils;

/**
 */
@Wire
@SymbolicName("zutubi.subversionConfigurationCheckHandler")
public class SubversionConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<SubversionConfiguration>
{
    private ScmClientFactory scmClientFactory;

    public void test(SubversionConfiguration configuration) throws ScmException
    {
        SubversionClient client = null;
        try
        {
            client = (SubversionClient) scmClientFactory.createClient(configuration);
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
