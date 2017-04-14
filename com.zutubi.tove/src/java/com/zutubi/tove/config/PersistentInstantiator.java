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

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.Instantiator;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

/**
 * Instantiator that is used when refreshing persistent instance caches in
 * the {@link ConfigurationTemplateManager}.  Handles caching and setting of
 * the handle and path on configuration objects.
 */
public class PersistentInstantiator implements Instantiator
{
    private String templateOwnerPath;
    private String path;
    private InstanceCache cache;
    private ReferenceResolver referenceResolver;
    private ConfigurationTemplateManager configurationTemplateManager;

    public PersistentInstantiator(String templateOwnerPath, String path, InstanceCache cache, ReferenceResolver referenceResolver, ConfigurationTemplateManager configurationTemplateManager)
    {
        this.templateOwnerPath = templateOwnerPath;
        this.path = path;
        this.cache = cache;
        this.referenceResolver = referenceResolver;
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public Object instantiate(String propertyPath, boolean relative, Type type, Object data) throws TypeException
    {
        if (relative)
        {
            propertyPath = PathUtils.getPath(path, propertyPath);
        }

        Object instance = cache.get(propertyPath, true);
        if (instance == null)
        {
            PersistentInstantiator childInstantiator = new PersistentInstantiator(configurationTemplateManager.getTemplateOwnerPath(propertyPath), propertyPath, cache, referenceResolver, configurationTemplateManager);
            instance = type.instantiate(data, childInstantiator);

            if (instance != null)
            {
                // If this is a newly-created Configuration (as opposed to a
                // reference), we need to initialise, cache and validate it.
                if (type instanceof ComplexType && instance instanceof Configuration)
                {
                    configurationTemplateManager.wireIfRequired((Configuration) instance);

                    Configuration configuration = (Configuration) instance;

                    Record record = (Record) data;
                    for(String key: record.metaKeySet())
                    {
                        configuration.putMeta(key, record.getMeta(key));
                    }
                    
                    configuration.setConfigurationPath(propertyPath);
                    configuration.setConcrete(configurationTemplateManager.isConcrete(propertyPath));

                    cache.put(propertyPath, configuration, configuration.isConcrete());
                }

                type.initialise(instance, data, childInstantiator);
            }
        }

        return instance;
    }

    public Configuration resolveReference(long toHandle) throws TypeException
    {
        return referenceResolver.resolveReference(templateOwnerPath, toHandle, this, path);
    }
}
