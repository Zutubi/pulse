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

package com.zutubi.pulse.master.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Models a resource with information about what versions are available on what agents.
 */
public class ResourceModel
{
    private String name;
    private Map<String, ResourceVersionModel> versionsById = new TreeMap<>();

    public ResourceModel(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public List<ResourceVersionModel> getVersions()
    {
        if (versionsById.isEmpty())
        {
            return null;
        }
        else
        {
            return new ArrayList<>(versionsById.values());
        }
    }

    public ResourceVersionModel getOrAddVersion(String versionId)
    {
        ResourceVersionModel version = versionsById.get(versionId);
        if (version == null)
        {
            version = new ResourceVersionModel(versionId);
            versionsById.put(versionId, version);
        }

        return version;
    }
}
