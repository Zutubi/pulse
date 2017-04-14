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

package com.zutubi.pulse.master.bootstrap.velocity;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.File;

/**
 * A velocity resource file loader that uses the application installation
 * directories to lookup velocity templates.
 */
public class PulseTemplateVelocityResourceLoader extends FileResourceLoader
{
    private MasterConfigurationManager configManager;

    public PulseTemplateVelocityResourceLoader()
    {
        // since velocity does not provide an object creation hook,
        // we need to handle the autowiring ourselves.
        SpringComponentContext.autowire(this);
    }

    /**
     * Retrieve a comma separated list of the systems template paths. These paths
     * are represented as absolute paths.
     *
     * @return the template paths added by this loader
     */
    public String getFullTemplatePath()
    {
        SystemPaths paths = configManager.getSystemPaths();

        StringBuffer result = new StringBuffer();
        String sep = "";
        for (File path : paths.getTemplateRoots())
        {
            result.append(sep);
            result.append(path.getAbsolutePath());
            sep = ",";
        }
        return result.toString();
    }

    public void init(ExtendedProperties configuration)
    {
        // NOTE: the path can be a comma separated list of paths... 
        configuration.setProperty("path", getFullTemplatePath());
        super.init(configuration);
    }

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }
}
