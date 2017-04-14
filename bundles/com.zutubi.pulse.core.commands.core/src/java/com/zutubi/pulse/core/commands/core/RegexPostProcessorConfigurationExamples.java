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

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.tove.config.api.ConfigurationExample;

import java.util.regex.Pattern;

/**
 * Example configurations for regex.pp.
 */
public class RegexPostProcessorConfigurationExamples
{
    public ConfigurationExample getCompiler()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("compile.pp");
        pp.addErrorRegexes("^.+:[0-9]+: error:");
        pp.addWarningRegexes("^.+:[0-9]+: warning:");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }

    public ConfigurationExample getExclusions()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("errors.pp");
        pp.setFailOnError(false);
        pp.setLeadingContext(3);
        pp.setTrailingContext(5);
        RegexPatternConfiguration pattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("[Ee]rror"));
        pattern.addExclusion("MyError.java");
        pattern.addExclusion("terror.txt");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }
}