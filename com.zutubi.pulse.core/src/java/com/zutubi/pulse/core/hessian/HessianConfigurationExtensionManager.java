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

package com.zutubi.pulse.core.hessian;

import com.zutubi.pulse.core.plugins.AbstractExtensionManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.HashMap;
import java.util.Map;

/**
 */
//This may be used by hessian, but the functionality is something that can easily be supported by the
//configuration extension manager.
public class HessianConfigurationExtensionManager extends AbstractExtensionManager
{
    private final Map<String, String> contributorMapping = new HashMap<String, String>();

    public void init()
    {
        super.init();
    }

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.config";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String className = config.getAttribute("class");
        String contributor = extension.getContributor().getName();

        contributorMapping.put(className, contributor);
    }

    public void removeExtension(IExtension iExtension, Object[] objects)
    {

    }

    public String getContributor(String className)
    {
        return contributorMapping.get(className);
    }
}
