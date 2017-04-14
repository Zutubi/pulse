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

package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes a plugged-in post-processor.
 */
public class PostProcessorDescriptor
{
    private String name;
    private String displayName;
    private boolean contributeDefault;
    private Class<? extends PostProcessorConfiguration> clazz;
    private List<String> templateFragments = new LinkedList<String>();

    public PostProcessorDescriptor(String name, String displayName, boolean contributeDefault, Class<? extends PostProcessorConfiguration> clazz)
    {
        this.name = name;
        this.displayName = displayName;
        this.contributeDefault = contributeDefault;
        this.clazz = clazz;
    }

    void addTemplateFragment(String template)
    {
        templateFragments.add(template);
    }

    public String getName()
    {
        return name;
    }

    public boolean isContributeDefault()
    {
        return contributeDefault;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public Class<? extends PostProcessorConfiguration> getClazz()
    {
        return clazz;
    }

    public List<String> getTemplateFragments()
    {
        return templateFragments;
    }
}
