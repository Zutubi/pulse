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
import java.util.List;

/**
 * <class-comment/>
 */
public interface SystemPaths
{
    /**
     * The system root directory, under which all system files should be
     * stored.  This is split into some more specific directories below.
     *
     * @return the directory to use as the system root
     */
    File getSystemRoot();

    /**
     * The www root directory.  Used as the base for deploying a web
     * application.
     *
     * @return the directory to use as the root for the web app
     */
    File getContentRoot();

    /**
     * The template root directories.  All velocity templates are nested under
     * these directories.
     *
     * @return a list of the directories that contain all velocity templates
     */
    List<File> getTemplateRoots();

    /**
     * Returns the system configuration directory.  Configuration files in
     * this directory are not normally edited by the user.  An example is
     * logging configuration, which may be edited in special circumstances
     * (e.g. debugging)
     *
     * @return the directory to use for system configuration files
     */
    File getConfigRoot();

    /**
     * Returns the directory to use for log files.
     *
     * @return the logs directory
     */
    File getLogRoot();

    /**
     * Returns the root directory of the systems temporary space.
     *
     * @return the directory used for temporary files.
     */
    File getTmpRoot();

}
