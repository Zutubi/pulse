package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * An implementation of the ScmClientFactory that delegates the creation of the clients to the
 * ScmClientFactory classes registered with the configuration.
 */
public class DelegateScmClientFactory implements ScmClientFactory<ScmConfiguration>
{
    private ScmExtensionManager scmExtensionManager;
    
    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        ScmClientFactory factory = scmExtensionManager.getClientFactory(config);
        @SuppressWarnings({"unchecked"})
        ScmClient client = factory.createClient(config);
        return client;
    }

    public void setScmExtensionManager(ScmExtensionManager scmExtensionManager)
    {
        this.scmExtensionManager = scmExtensionManager;
    }
}
