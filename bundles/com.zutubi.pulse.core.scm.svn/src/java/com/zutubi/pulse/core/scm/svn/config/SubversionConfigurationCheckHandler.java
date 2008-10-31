package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.tove.config.ConfigurationCheckHandlerSupport;

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
            if (client != null)
            {
                client.close();
            }
        }
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
