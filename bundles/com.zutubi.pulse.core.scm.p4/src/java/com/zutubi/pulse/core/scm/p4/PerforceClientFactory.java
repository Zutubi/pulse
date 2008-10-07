package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.p4.config.PerforceConfiguration;

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
