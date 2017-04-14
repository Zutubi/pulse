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

package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.PostProcessorGroupConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.OutputPostProcessorConfigurationSupport;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;

/**
 * Configuration for instances if {@link MavenPostProcessor}.
 */
@SymbolicName("zutubi.mavenPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnError", "failOnWarning"})
public class MavenPostProcessorConfiguration extends OutputPostProcessorConfigurationSupport
{
    private static final String JUNIT_PROCESSOR_NAME = "junit.summary";
    private static final String MAVEN_ERRORS_PROCESSOR_NAME = "maven.errors";

    private static final String[] errorRegexs = new String[]{
            ".*BUILD FAILED.*",
            "Basedir.*does not exist",
            ".*The build cannot continue because of the following unsatisfied dependencies:.*"
    };

    public MavenPostProcessorConfiguration()
    {
        super(MavenPostProcessor.class);
    }

    public MavenPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }

    public PostProcessorGroupConfiguration asGroup()
    {
        PostProcessorGroupConfiguration group = new PostProcessorGroupConfiguration();
        
        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        group.getProcessors().put(JUNIT_PROCESSOR_NAME, new JUnitSummaryPostProcessorConfiguration(JUNIT_PROCESSOR_NAME));

        // Regex for error patterns from maven itself
        RegexPostProcessorConfiguration maven = new RegexPostProcessorConfiguration(MAVEN_ERRORS_PROCESSOR_NAME);
        maven.addErrorRegexes(errorRegexs);

        if (!SystemUtils.IS_WINDOWS)
        {
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        group.getProcessors().put(MAVEN_ERRORS_PROCESSOR_NAME, maven);

        for (PostProcessorConfiguration child: group.getProcessors().values())
        {
            OutputPostProcessorConfigurationSupport outputChild = (OutputPostProcessorConfigurationSupport) child;
            outputChild.setFailOnError(isFailOnError());
            outputChild.setFailOnWarning(isFailOnWarning());
        }
        return group;
    }
}
