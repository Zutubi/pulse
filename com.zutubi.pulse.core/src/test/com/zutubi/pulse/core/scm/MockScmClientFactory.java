package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.MockScmConfiguration;


/**
 *
 *
 */
public class MockScmClientFactory implements ScmClientFactory<MockScmConfiguration>
{
    public ScmClient createClient(MockScmConfiguration config) throws ScmException
    {
        return new MockScmClient();
    }

}
