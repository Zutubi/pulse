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

package com.zutubi.pulse.master.tove.config.core;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.MapOptionProvider;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends MapOptionProvider
{
    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        // There is always an 'empty' option.
        return null;
    }

    public Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        List<String> resourceVersions = new ArrayList<>();
        if (context.getExistingInstance() != null)
        {
            resourceVersions.addAll(((ResourceConfiguration) context.getExistingInstance()).getVersions().keySet());
        }
        Collections.sort(resourceVersions, new Sort.StringComparator());

        Map<String, String> versions = new HashMap<>(resourceVersions.size() + 1);
        versions.put("", "[none]");
        for(String version: resourceVersions)
        {
            versions.put(version, version);
        }
        
        return versions;
    }
}
