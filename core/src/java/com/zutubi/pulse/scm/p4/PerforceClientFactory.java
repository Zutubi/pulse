package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.scm.ScmClientFactory;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.p4.config.PerforceConfiguration;

/**
 *
 *
 */
public class PerforceClientFactory implements ScmClientFactory<PerforceConfiguration>
{
    public ScmClient createClient(PerforceConfiguration config) throws ScmException
    {
        PerforceClient client = new PerforceClient(config.getPort(), config.getUser(), config.getPassword(), config.getSpec());
        client.setExcludedPaths(config.getFilterPaths());
        return client;
    }
}
