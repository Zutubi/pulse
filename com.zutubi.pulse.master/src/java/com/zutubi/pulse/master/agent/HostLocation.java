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

package com.zutubi.pulse.master.agent;

/**
 * Abstracts the notion of a host location shared by hosts and agents.
 */
public interface HostLocation
{
    /**
     * Indicates if this is a remote host.
     *
     * @return true if the host is remote, false if it is the local machine
     */
    boolean isRemote();

    /**
     * Returns the name of the host for remote locations.
     *
     * @return the hostname of the remote location
     */
    String getHostName();

    /**
     * Returns the port for remote locations.
     *
     * @return the port number of the remote location
     */
    int getPort();

    /**
     * Indicates if the remote location is listening for a secure connection.
     *
     * @return true if the location uses SSL, false otherwise
     */
    boolean isSsl();
}
