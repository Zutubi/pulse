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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Mimics the configuration type for the root of a tove file.
 */
@SymbolicName("root")
public class RootConfiguration extends AbstractConfiguration
{
    @Addable("mixed")
    private Map<String, MixedConfiguration> mixers = new HashMap<String, MixedConfiguration>();
    @Addable("required")
    private Map<String, RequiredPropertiesConfiguration> requireds = new HashMap<String, RequiredPropertiesConfiguration>();

    public Map<String, MixedConfiguration> getMixers()
    {
        return mixers;
    }

    public void setMixers(Map<String, MixedConfiguration> mixers)
    {
        this.mixers = mixers;
    }

    public Map<String, RequiredPropertiesConfiguration> getRequireds()
    {
        return requireds;
    }

    public void setRequireds(Map<String, RequiredPropertiesConfiguration> requireds)
    {
        this.requireds = requireds;
    }
}
