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

package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.Message;

import java.io.File;

/**
 * The ivy implementation of the dependency manager interface.
 */
public class IvyManager
{
    private File cacheBase;

    static
    {
        // redirect the default logging to our own logging system.
        Message.setDefaultLogger(new IvyMessageLoggerAdapter());
    }

    public void init()
    {
    }

    // Used by Spring
    @SuppressWarnings("UnusedDeclaration")
    public void setDataDir(File dataDir)
    {
        this.cacheBase = new File(dataDir, "cache");
    }

    /**
     * Creates a new Ivy client with default configuration for the given repository.
     * 
     * @param repositoryBase path to the base of the repository to use
     * @param id a unique identifier that this client may use to avoid conflicts with other clients
     * @return the new client
     * @throws Exception if there is an error configuring the client
     */
    public IvyClient createIvyClient(String repositoryBase, long id) throws Exception
    {
        return createIvyClient(new IvyConfiguration(repositoryBase), id);
    }

    /**
     * Creates a new Ivy client with the given configuration.
     *
     * @param configuration configuration for the client
     * @param id a unique identifier that this client may use to avoid conflicts with other clients
     * @return the new client
     * @throws Exception if there is an error configuring the client
     */
    public IvyClient createIvyClient(IvyConfiguration configuration, long id) throws Exception
    {
        if (cacheBase != null && configuration.getCacheBase() == null)
        {
            // Give each ivy client a unique cache base directory so that they
            // can be cleaned up without interfering with each other.
            File clientCacheBase = new File(cacheBase, "ivy-" + id);
            configuration.setCacheBase(clientCacheBase);
        }
        return new IvyClient(configuration);
    }
}
