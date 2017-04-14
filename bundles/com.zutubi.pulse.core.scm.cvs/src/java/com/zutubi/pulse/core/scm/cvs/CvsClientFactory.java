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

package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.config.CvsConfiguration;
import com.zutubi.util.bean.ObjectFactory;

/**
 * Scm client factory implementation that uses a CvsConfiguration to create a cvs client.
 */
public class CvsClientFactory implements ScmClientFactory<CvsConfiguration>
{
    private ObjectFactory objectFactory;

    public ScmClient createClient(CvsConfiguration config) throws ScmException
    {
        try
        {
            CvsClient client = objectFactory.buildBean(CvsClient.class,
                            config.getRoot(),
                            config.getModule(),
                            config.getPassword(),
                            config.getBranch()
            );
            client.setFilterPaths(config.getIncludedPaths(), config.getExcludedPaths());
            return client;
        }
        catch (Exception e)
        {
            throw new ScmException(e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
