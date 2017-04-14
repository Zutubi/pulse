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

package com.zutubi.pulse.servercore.jetty;

import org.eclipse.jetty.server.Server;

import java.io.IOException;

/**
 * The server configuration handler is used to hold the configuration
 * logic for jetty server instances.
 */
public interface ServerConfigurationHandler
{
    /**
     * Configure the server instance.
     *
     * @param server instance to be configured.
     *
     * @throws IOException on error
     */
    void configure(Server server) throws IOException;
}
