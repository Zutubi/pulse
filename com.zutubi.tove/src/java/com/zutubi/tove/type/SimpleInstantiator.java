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

package com.zutubi.tove.type;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ReferenceResolver;
import com.zutubi.tove.config.api.Configuration;

/**
 * A property instantiator that applies no extra logic to the instantiation
 * process.
 */
public class SimpleInstantiator implements Instantiator
{
    private String templateOwnerPath;
    private ReferenceResolver referenceResolver;
    private ConfigurationTemplateManager configurationTemplateManager;

    /**
     * Create a new instantiator for building throw-away instances.
     *
     * @param templateOwnerPath if in a templated scope, the item of the
     *                          templated collection that owns the object being
     *                          instantiated, otherwise null
     * @param referenceResolver used to resolve references during instantiation
     * @param configurationTemplateManager required resource
     */
    public SimpleInstantiator(String templateOwnerPath, ReferenceResolver referenceResolver, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.templateOwnerPath = templateOwnerPath;
        this.referenceResolver = referenceResolver;
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public Object instantiate(String property, boolean relative, Type type, Object data) throws TypeException
    {
        return instantiate(type, data);
    }

    public Object instantiate(Type type, Object data) throws TypeException
    {
        Object instance = type.instantiate(data, this);
        if (instance != null)
        {
            if (instance instanceof Configuration)
            {
                configurationTemplateManager.wireIfRequired((Configuration) instance);
            }

            type.initialise(instance, data, this);
        }
        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return referenceResolver.resolveReference(templateOwnerPath, toHandle, this, null);
    }
}
