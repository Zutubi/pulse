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

package com.zutubi.pulse.core.commands.nant;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example nant post-processors.
 */
public class NAntPostProcessorConfigurationExamples
{
    private static final String NAME = "nant.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getContextNoFailOnError()
    {
        NAntPostProcessorConfiguration pp = createEmpty();
        pp.setLeadingContext(10);
        pp.setFailOnError(false);
        return new ConfigurationExample(NAME, pp);
    }

    private NAntPostProcessorConfiguration createEmpty()
    {
        NAntPostProcessorConfiguration pp = new NAntPostProcessorConfiguration(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}
