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
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeException;

/**
 * Common interface for configuration type registries.  Actual registries need to be provided outside Tove, built on
 * top of a {@link com.zutubi.tove.type.TypeRegistry}.
 */
public interface ConfigurationRegistry
{
    <T extends Configuration> CompositeType registerConfigurationType(Class<T> clazz) throws TypeException;
    CompositeType getConfigurationCheckType(CompositeType type);

}
