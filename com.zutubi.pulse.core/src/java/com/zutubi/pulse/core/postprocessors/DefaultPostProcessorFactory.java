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

package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory;

/**
 * Default implementation of {@link com.zutubi.pulse.core.postprocessors.api.PostProcessorFactory},
 * which uses the object factory to build processors.
 */
public class DefaultPostProcessorFactory extends ConfiguredInstanceFactory<PostProcessor, PostProcessorConfiguration> implements PostProcessorFactory
{
    protected Class<? extends PostProcessor> getType(PostProcessorConfiguration configuration)
    {
        return configuration.processorType();
    }
}
