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

package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.type.CompositeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores top-level type definitions for a specific type of tove file.
 */
public class TypeDefinitions
{
    private Map<String, CompositeType> nameToType = new HashMap<String, CompositeType>();
    private Map<CompositeType, String> typeToName = new HashMap<CompositeType, String>();

    public void register(String name, CompositeType type)
    {
        nameToType.put(name, type);
        typeToName.put(type, name);
    }

    public CompositeType unregister(String name)
    {
        CompositeType type = nameToType.remove(name);
        if (type != null)
        {
            typeToName.remove(type);
        }
        
        return type;
    }

    public boolean hasType(String name)
    {
        return getType(name) != null;
    }

    public CompositeType getType(String name)
    {
        return nameToType.get(name);
    }

    public boolean hasName(CompositeType type)
    {
        return getName(type) != null;
    }

    public String getName(CompositeType compositeType)
    {
        return typeToName.get(compositeType);
    }
}
