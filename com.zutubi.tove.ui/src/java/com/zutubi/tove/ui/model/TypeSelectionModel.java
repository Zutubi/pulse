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

package com.zutubi.tove.ui.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.util.adt.Pair;

import java.util.List;

/**
 * Models an unconfigured property that has an undetermined type -- i.e. property type itself is
 * abstract with multiple possible extensions.
 */
@JsonTypeName("type-selection")
public class TypeSelectionModel extends ConfigModel
{
    private CompositeTypeModel type;
    private List<Pair<Integer, String>> configuredDescendants;

    public TypeSelectionModel(String key, String label)
    {
        super(null, key, label, true);
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public void setType(CompositeTypeModel type)
    {
        this.type = type;
    }

    public List<Pair<Integer, String>> getConfiguredDescendants()
    {
        return configuredDescendants;
    }

    public void setConfiguredDescendants(List<Pair<Integer, String>> configuredDescendants)
    {
        this.configuredDescendants = configuredDescendants;
    }
}
