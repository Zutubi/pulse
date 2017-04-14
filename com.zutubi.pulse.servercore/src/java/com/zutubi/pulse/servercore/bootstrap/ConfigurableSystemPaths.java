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
 * A convenience object that allows for the System and User paths to be specified
 */
public class ConfigurableSystemPaths implements SystemPaths
{
    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File logRoot;
    private File tmpRoot;
    private List<File> templateRoots;

    public File getSystemRoot()
    {
        return systemRoot;
    }

    public File getContentRoot()
    {
        return contentRoot;
    }

    public File getConfigRoot()
    {
        return configRoot;
    }

    public File getLogRoot()
    {
        return logRoot;
    }

    public List<File> getTemplateRoots()
    {
        return templateRoots;
    }

    public File getTmpRoot()
    {
        return tmpRoot;
    }

    public void setTemplateRootStrings(List<String> roots)
    {
        templateRoots = new LinkedList<File>();
        for (String root : roots)
        {
            templateRoots.add(new File(root));
        }
    }

    public void setSystemRootString(String systemRoot)
    {
        this.systemRoot = new File(systemRoot);
    }

    public void setContentRootString(String contentRoot)
    {
        this.contentRoot = new File(contentRoot);
    }

    public void setConfigRootString(String configRoot)
    {
        this.configRoot = new File(configRoot);
    }

    public void setLogRootString(String logRoot)
    {
        this.logRoot = new File(logRoot);
    }

    public void setTmpRootString(String tmpRoot)
    {
        this.tmpRoot = new File(tmpRoot);
    }
}
