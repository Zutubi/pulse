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

package com.zutubi.pulse.core.postprocessors.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Support base class to configure an instance of {@link OutputPostProcessorSupport}.
 */
@SymbolicName("zutubi.postProcessorConfigSupport")
public abstract class PostProcessorConfigurationSupport extends AbstractNamedConfiguration implements PostProcessorConfiguration
{
    private Class<? extends PostProcessor> postProcessorType;

    protected PostProcessorConfigurationSupport(Class<? extends PostProcessor> postProcessorType)
    {
        this.postProcessorType = postProcessorType;
    }

    public Class<? extends PostProcessor> processorType()
    {
        return postProcessorType;
    }
}