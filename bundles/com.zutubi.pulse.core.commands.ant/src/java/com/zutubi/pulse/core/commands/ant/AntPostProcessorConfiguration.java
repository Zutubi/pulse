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

package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned regular expression post-processor configuration for ant output.
 * Attempts to capture features from Ant itself (e.g. "BUILD FAILED") and from
 * commonly-used tasks (e.g. javac).
 */
@SymbolicName("zutubi.antPostProcessorConfig")
public class AntPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    private static final String ERROR_PATTERN = "\\[javac\\] .*:[0-9]+:";
    private static final String WARNING_PATTERN = "\\[javac\\] .*:[0-9]+: warning";

    public AntPostProcessorConfiguration()
    {
        // Add our built-in patterns.

        // Ant's own build failed messages
        // Ant gives different failure messages in different cases, for
        // example:
        //
        // jsankey@shiny:~/svn/pulse/trunk$ ant -f nosuchfile
        // Buildfile: nosuchfile does not exist!
        // Build failed
        //
        // versus:
        //
        // jsankey@shiny:~/svn/pulse/trunk$ ant nosuchtarget
        // Buildfile: build.xml
        //
        // BUILD FAILED
        // Target `nosuchtarget' does not exist in this project.
        //
        // Total time: 0 seconds
        addErrorRegexes("^Build failed|^BUILD FAILED");

        // javac task compiler messages
        RegexPatternConfiguration pattern = new RegexPatternConfiguration();
        pattern.setCategory(Feature.Level.ERROR);
        pattern.setExpression(ERROR_PATTERN);
        pattern.getExclusions().add(WARNING_PATTERN);
        getPatterns().add(pattern);

        pattern = new RegexPatternConfiguration();
        pattern.setCategory(Feature.Level.WARNING);
        pattern.setExpression(WARNING_PATTERN);
        getPatterns().add(pattern);

        setLeadingContext(5);
        setTrailingContext(5);
    }

    public AntPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
