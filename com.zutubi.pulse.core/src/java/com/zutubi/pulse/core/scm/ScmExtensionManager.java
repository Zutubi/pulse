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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.AbstractExtensionManager;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.patch.DefaultPatchFormatFactory;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension manager for managing scm implementations
 */
public class ScmExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(ScmExtensionManager.class);

    private ObjectFactory objectFactory;
    private DefaultPatchFormatFactory patchFormatFactory;
    private Map<Class, ScmClientFactory> factories = new HashMap<Class, ScmClientFactory>();

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.scms";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        // Extension point configuration:
        //     <extension point="com.zutubi.pulse.core.config">
        //         <config class="..."/>
        //     </extension>
        //
        //     <extension point="com.zutubi.pulse.core.scms">
        //         <scm name="..." factory-class="..." patch-format="..." working-copy-class="..."/>
        //     </extension>
        String name = config.getAttribute("name");
        if (!StringUtils.stringSet(name))
        {
            LOG.severe("Attempt to register SCM with no name: ignoring");
            return;
        }

        try
        {
            Class configClazz = getConfigClass(extension, name);
            Class factoryClazz = getFactoryClass(extension, config);

            // The working copy and patch format are optional (may be null).
            Class wcClazz = getWorkingCopyClass(extension, config);
            String patchFormat = getPatchFormat(config, wcClazz);

            if (PluginManager.VERBOSE_EXTENSIONS)
            {
                System.out.println(String.format("Adding SCM: %s -> (%s, %s, %s, %s)", name,
                        configClazz.getName(),
                        factoryClazz.getName(),
                        wcClazz == null ? "<none>" : wcClazz.getName(),
                        patchFormat == null ? "<none>" : patchFormat));
            }

            try
            {
                registerClientFactory(configClazz, factoryClazz);
                if (wcClazz != null)
                {
                    WorkingCopyFactory.registerType(name, wcClazz);
                    if (patchFormat != null)
                    {
                        patchFormatFactory.registerScm(name, patchFormat);
                    }
                }
            }
            catch (Throwable e)
            {
                LOG.warning(e);
                handleExtensionError(extension, e);
            }
        }
        catch (Exception e)
        {
            LOG.severe("While registering SCM '" + name + "': " + e.getMessage(), e);
            handleExtensionError(extension, e);
        }
    }

    public ScmClientFactory getClientFactory(ScmConfiguration config)
    {
        return factories.get(config.getClass());
    }

    <T extends ScmConfiguration> void registerClientFactory(Class<T> configType, Class<? extends ScmClientFactory<T>> factoryType) throws ScmException
    {
        try
        {
            ScmClientFactory<T> factory = objectFactory.buildBean(factoryType);
            factories.put(configType, factory);
        }
        catch (Exception e)
        {
            throw new ScmException(e);
        }
    }

    private Class getConfigClass(IExtension extension, String name)
    {
        List<IConfigurationElement> configElements = getConfigElements(extension);
        if (configElements.size() != 1)
        {
            throw new PulseRuntimeException(String.format("Expected one configuration extension but instead found %d", configElements.size()));
        }

        IConfigurationElement configElement = configElements.get(0);
        String configClassName = configElement.getAttribute("class");
        Class configClazz = loadClass(extension, configClassName);

        if (!ScmConfiguration.class.isAssignableFrom(configClazz))
        {
            throw new PulseRuntimeException(String.format("Configuration class '%s' does not extend '%s'", configClassName, ScmConfiguration.class.getName()));
        }

        ScmConfiguration configInstance;
        try
        {
            configInstance = (ScmConfiguration) configClazz.newInstance();
        }
        catch (Exception e)
        {
            throw new PulseRuntimeException("Could not instantiate config class: " + e.getMessage(), e);
        }

        String type = configInstance.getType();
        if (!type.equals(name))
        {
            throw new PulseRuntimeException(String.format("Method %s.getType() is expected to return '%s' but instead returned '%s'", configClassName, name, type));
        }

        return configClazz;
    }

    private Class getFactoryClass(IExtension extension, IConfigurationElement config)
    {
        String factoryClassName = config.getAttribute("factory-class");
        if (factoryClassName == null)
        {
            throw new PulseRuntimeException("No factory class specified");
        }

        Class factoryClazz = loadClass(extension, factoryClassName);
        if (!ScmClientFactory.class.isAssignableFrom(factoryClazz))
        {
            throw new PulseRuntimeException(String.format("Factory class '%s' does not implement '%s'", factoryClassName, ScmClientFactory.class.getName()));
        }

        return factoryClazz;
    }

    private Class getWorkingCopyClass(IExtension extension, IConfigurationElement config)
    {
        String wcClassName = config.getAttribute("working-copy-class");
        if (wcClassName == null)
        {
            return null;
        }

        Class wcClazz = loadClass(extension, wcClassName);
        if (!WorkingCopy.class.isAssignableFrom(wcClazz))
        {
            throw new PulseRuntimeException((String.format("Working copy class '%s' does not implement '%s'", wcClassName, WorkingCopy.class.getName())));
        }

        return wcClazz;
    }

    private String getPatchFormat(IConfigurationElement config, Class wcClazz)
    {
        String patchFormat = config.getAttribute("patch-format");
        if (patchFormat != null && wcClazz == null)
        {
            throw new PulseRuntimeException("Patch file format given, but no working copy class specified.");
        }

        // To support standard patch files, there must be a working copy
        // implementing WorkingCopyStatusBuilder.
        if (DefaultPatchFormatFactory.FORMAT_STANDARD.equals(patchFormat) && !WorkingCopyStatusBuilder.class.isAssignableFrom(wcClazz))
        {
            throw new PulseRuntimeException(String.format("Standard patch file format requested, but working copy class '%s' does not implement '%s'", wcClazz.getName(), WorkingCopyStatusBuilder.class.getName()));
        }

        return patchFormat;
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        // Uninstall not currently supported
        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.println("extension removed: " + extension.getContributor().getName());
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setPatchFormatFactory(DefaultPatchFormatFactory patchFormatFactory)
    {
        this.patchFormatFactory = patchFormatFactory;
    }
}
