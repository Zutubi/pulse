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

import com.zutubi.tove.config.api.ConfigurationExample;

import static java.util.Arrays.asList;

/**
 * Example configurations for regex-test.pp.
 */
public class RegexTestPostProcessorConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        RegexTestPostProcessorConfiguration pp = new RegexTestPostProcessorConfiguration("sample-regex-test");
        pp.setStatusGroup(1);
        pp.setNameGroup(2);
        pp.setPassStatus(asList("PASS"));
        pp.setFailureStatus(asList("FAIL"));
        pp.setRegex("(.*) - .* - (.*)");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }
}
