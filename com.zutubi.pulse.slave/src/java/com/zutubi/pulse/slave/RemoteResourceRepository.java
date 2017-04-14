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

package com.zutubi.pulse.slave;

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

/**
 * A resource repository that lives remotely on the master server.
 */
public class RemoteResourceRepository extends ResourceRepositorySupport
{
    private static final Logger LOG = Logger.getLogger(RemoteResourceRepository.class);

    private long handle;
    private MasterService masterProxy;
    private ServiceTokenManager serviceTokenManager;

    public RemoteResourceRepository(long handle, MasterService masterProxy, ServiceTokenManager serviceTokenManager)
    {
        this.handle = handle;
        this.masterProxy = masterProxy;
        this.serviceTokenManager = serviceTokenManager;
    }

    public ResourceConfiguration getResource(String name)
    {
        try
        {
            return masterProxy.getResource(serviceTokenManager.getToken(), handle, name);
        }
        catch (RuntimeException e)
        {
            LOG.severe(e);
            throw new BuildException("Unable to retrieve details of resource '" + name + "' from master: " + e.getMessage());
        }
    }
}
