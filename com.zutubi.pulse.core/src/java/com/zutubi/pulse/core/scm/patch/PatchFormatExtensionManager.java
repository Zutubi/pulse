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

package com.zutubi.pulse.core.scm.patch;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.AbstractExtensionManager;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for pluggable patch formats.
 */
public class PatchFormatExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(PatchFormatExtensionManager.class);

    private DefaultPatchFormatFactory patchFormatFactory;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.patchformats";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        if (!StringUtils.stringSet(name))
        {
            LOG.severe("Ignoring patch format with no name");
        }

        try
        {
            @SuppressWarnings({"unchecked"})
            Class<? extends PatchFormat> clazz = getFormatClass(extension, config);

            if (PluginManager.VERBOSE_EXTENSIONS)
            {
                System.out.printf("Adding Patch Format: %s -> %s\n", name, clazz.getName());
            }

            patchFormatFactory.registerFormatType(name, clazz);
            tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
        }
        catch (Exception e)
        {
            LOG.severe("While registering patch format '" + name + "': " + e.getMessage(), e);
            handleExtensionError(extension, e);
        }
    }

    private Class getFormatClass(IExtension extension, IConfigurationElement config)
    {
        String className = config.getAttribute("class");
        Class clazz = loadClass(extension, className);
        if(clazz == null)
        {
            throw new PulseRuntimeException(String.format("Class '%s' does not exist", className));
        }

        if (!PatchFormat.class.isAssignableFrom(clazz))
        {
            throw new PulseRuntimeException(String.format("Class '%s' does not implement PatchFormat", className));
        }

        try
        {
            clazz.getConstructor();
        }
        catch (NoSuchMethodException e)
        {
            throw new PulseRuntimeException(String.format("Class '%s' does not have a no-argument constructor", className));
        }
        
        return clazz;
    }

    public void removeExtension(IExtension iExtension, Object[] objects)
    {
        for (Object o : objects)
        {
            patchFormatFactory.unregisterFormatType((String) o);
        }
    }

    public void setPatchFormatFactory(DefaultPatchFormatFactory patchFormatFactory)
    {
        this.patchFormatFactory = patchFormatFactory;
    }
}
