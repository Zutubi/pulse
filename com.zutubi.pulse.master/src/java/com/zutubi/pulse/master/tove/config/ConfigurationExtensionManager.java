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

package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.plugins.AbstractExtensionManager;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * An extension point manager that handles the registration of all
 * configuration types.  This manager is initialised earlier than most
 * extension managers to ensure the types are available.
 */
public class ConfigurationExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationExtensionManager.class);
    
    private ConfigurationRegistry configurationRegistry;

    public void init()
    {
        // Don't register with plugin manager (i.e. don't call super) as we
        // take care of our own initialisation.
        initialiseExtensions();
    }

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.config";
    }

    @SuppressWarnings({ "unchecked" })
    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String className = config.getAttribute("class");

        Class clazz = loadClass(extension, className);
        if(clazz != null)
        {
            try
            {
                configurationRegistry.registerConfigurationType(clazz);
            }
            catch (TypeException e)
            {
                LOG.warning(e);
                handleExtensionError(extension, e);
            }
        }
    }

    public void removeExtension(IExtension iExtension, Object[] objects)
    {
        // Do nothing.
    }


    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
