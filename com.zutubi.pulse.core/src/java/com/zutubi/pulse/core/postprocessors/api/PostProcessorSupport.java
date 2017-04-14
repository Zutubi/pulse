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

/**
 * Basic support for implementation of {@link PostProcessor}.  Stores the
 * configuration associated with the processor.
 *
 * @see OutputPostProcessorSupport
 * @see TestReportPostProcessorSupport
 */
public abstract class PostProcessorSupport implements PostProcessor
{
    private PostProcessorConfigurationSupport config;

    protected PostProcessorSupport(PostProcessorConfigurationSupport config)
    {
        this.config = config;
    }

    public PostProcessorConfigurationSupport getConfig()
    {
        return config;
    }
}