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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Configuration for instances of {@link com.zutubi.pulse.core.commands.core.PostProcessorGroup}.
 */
@SymbolicName("zutubi.postProcessorGroupConfig")
public class PostProcessorGroupConfiguration extends PostProcessorConfigurationSupport
{
    @Ordered
    private Map<String, PostProcessorConfiguration> processors = new LinkedHashMap<String, PostProcessorConfiguration>();

    public PostProcessorGroupConfiguration()
    {
        super(PostProcessorGroup.class);
    }

    public PostProcessorGroupConfiguration(Class<? extends PostProcessorGroup> postProcessorType)
    {
        super(postProcessorType);
    }

    public Map<String, PostProcessorConfiguration> getProcessors()
    {
        return processors;
    }

    public void setProcessors(Map<String, PostProcessorConfiguration> processors)
    {
        this.processors = processors;
    }

    public void addPostProcessor(PostProcessorConfiguration postProcessor)
    {
        processors.put(postProcessor.getName(), postProcessor);
    }
}