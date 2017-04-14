/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmContextFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.git.GitClient;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

/**
 * Tests connections to git repositories.
 */
@Wire
@SymbolicName("zutubi.gitConfigurationCheckHandler")
public class GitConfigurationCheckHandler extends AbstractConfigurationCheckHandler<GitConfiguration>
{
    private ScmClientFactory<? super GitConfiguration> scmClientFactory;
    private ScmContextFactory scmContextFactory;

    public void test(GitConfiguration configuration) throws ScmException
    {
        GitClient client = null;
        try
        {
            client = (GitClient) scmClientFactory.createClient(configuration);
            client.testConnection(scmContextFactory.createContext(configuration, client.getImplicitResource()));
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }
    }

    public void setScmClientFactory(ScmClientFactory<? super GitConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}

