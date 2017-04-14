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
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class DefaultSystemPaths implements SystemPaths
{
    private final File pulseHome;
    private final File versionHome;

    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File logRoot;
    private List<File> templateRoots;
    private File tmpRoot;

    public DefaultSystemPaths(File pulseHome, File versionHome)
    {
        this.pulseHome = pulseHome;
        this.versionHome = versionHome;
    }

    public File getSystemRoot()
    {
        if (systemRoot == null)
        {
            systemRoot = new File(versionHome, "system");
        }
        return systemRoot;
    }

    public File getContentRoot()
    {
        if (contentRoot == null)
        {
            contentRoot = new File(getSystemRoot(), "www");
        }
        return contentRoot;
    }

    public File getConfigRoot()
    {
        if (configRoot == null)
        {
            configRoot = new File(getSystemRoot(), "config");
        }
        return configRoot;
    }

    public File getLogRoot()
    {
        if (logRoot == null)
        {
            logRoot = new File(pulseHome, "logs");
        }
        return logRoot;
    }

    public List<File> getTemplateRoots()
    {
        if (templateRoots == null)
        {
            templateRoots = new LinkedList<File>();
            templateRoots.add(new File(getSystemRoot(), "templates"));
            templateRoots.add(new File(getSystemRoot(), "www"));
        }
        return templateRoots;
    }

    public File getTmpRoot()
    {
        if (tmpRoot == null)
        {
            tmpRoot = new File(getSystemRoot(), "tmp");
        }
        return tmpRoot;
    }
}
