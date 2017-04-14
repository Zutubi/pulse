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
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.LineBasedPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Configuration for instances of {@link com.zutubi.pulse.core.commands.core.RegexPostProcessor}.
 */
@SymbolicName("zutubi.regexPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnError", "failOnWarning", "leadingContext", "trailingContext", "joinOverlapping"})
public class RegexPostProcessorConfiguration extends LineBasedPostProcessorConfigurationSupport
{
    @Addable("pattern")
    private List<RegexPatternConfiguration> patterns = new LinkedList<RegexPatternConfiguration>();

    public RegexPostProcessorConfiguration()
    {
        this(RegexPostProcessor.class);
    }

    public RegexPostProcessorConfiguration(String name)
    {
        this(RegexPostProcessor.class, name);
    }

    public RegexPostProcessorConfiguration(Class<? extends RegexPostProcessor> postProcessorType)
    {
        super(postProcessorType);
    }

    public RegexPostProcessorConfiguration(Class<? extends RegexPostProcessor> postProcessorType, String name)
    {
        super(postProcessorType);
        setName(name);
    }

    public List<RegexPatternConfiguration> getPatterns()
    {
        return patterns;
    }

    public void setPatterns(List<RegexPatternConfiguration> patterns)
    {
        this.patterns = patterns;
    }

    public void addErrorRegexes(String... errorRegexs)
    {
        addRegexes(Feature.Level.ERROR, errorRegexs);
    }

    public void addWarningRegexes(String... warningRegexs)
    {
        addRegexes(Feature.Level.WARNING, warningRegexs);
    }

    public void addRegexes(Feature.Level level, String... regexes)
    {
        for (String errorRegex : regexes)
        {
            patterns.add(new RegexPatternConfiguration(level, Pattern.compile(errorRegex)));
        }
    }
}