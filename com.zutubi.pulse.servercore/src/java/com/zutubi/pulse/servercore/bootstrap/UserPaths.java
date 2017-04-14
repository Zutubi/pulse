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

package com.zutubi.pulse.servercore.bootstrap;

import java.io.File;

/**
 * User paths common to both the master and build agents.
 */
public interface UserPaths
{
    /**
     * The pulse data directory is the place where all of the users data is
     * stored.
     *
     * @return the data directory for all pulse data
     */
    File getData();

    /**
     * The user configuration directory.  Configuration files in this
     * directory are expected to be edited by the user.  A prime example is
     * basic system properties such as the port for the web application.
     *
     * @return the directory to use for user configuration files
     */
    File getUserConfigRoot();
}
