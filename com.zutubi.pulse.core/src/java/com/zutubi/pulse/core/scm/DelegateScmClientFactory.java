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
