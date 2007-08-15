package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.svn.config.SvnConfiguration;
import com.zutubi.pulse.core.scm.svn.SvnClient;

/**
 *
 *
 */
@SymbolicName("zutubi.svnConfigurationCheckHandler")
public class SvnConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<SvnConfiguration>
{
    private ScmClientFactory scmClientFactory;

    public void test(SvnConfiguration configuration) throws ScmException
    {
        SvnClient client = (SvnClient) scmClientFactory.createClient(configuration);
        client.testConnection();
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
