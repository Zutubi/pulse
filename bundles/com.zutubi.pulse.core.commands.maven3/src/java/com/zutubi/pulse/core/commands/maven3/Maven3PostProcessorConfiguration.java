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

package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.PostProcessorGroupConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Min;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configuration for instances of {@link Maven3PostProcessor}.
 */
@SymbolicName("zutubi.maven3PostProcessorConfig")
@Form(fieldOrder = {"name", "leadingContext", "trailingContext", "suppressedWarnings", "suppressedErrors"})
public class Maven3PostProcessorConfiguration extends AbstractNamedConfiguration implements PostProcessorConfiguration, Validateable
{
    private static final String JUNIT_PROCESSOR_NAME = "junit.summary";
    private static final String MAVEN_ERRORS_PROCESSOR_NAME = "maven.errors";

    @Min(0) @Wizard.Ignore
    private int leadingContext = 1;
    @Min(0) @Wizard.Ignore
    private int trailingContext = 2;
    @Addable(value = "suppress-error", attribute = "expression") @StringList @Wizard.Ignore
    private List<String> suppressedWarnings = new LinkedList<String>();
    @Addable(value = "suppress-warning", attribute = "expression") @StringList @Wizard.Ignore
    private List<String> suppressedErrors = new LinkedList<String>();

    public Maven3PostProcessorConfiguration()
    {
    }

    public Maven3PostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }

    public Class<? extends PostProcessor> processorType()
    {
        return Maven3PostProcessor.class;
    }

    public PostProcessorGroupConfiguration asGroup()
    {
        PostProcessorGroupConfiguration group = new PostProcessorGroupConfiguration();

        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        group.getProcessors().put(JUNIT_PROCESSOR_NAME, new JUnitSummaryPostProcessorConfiguration(JUNIT_PROCESSOR_NAME));

        RegexPatternConfiguration failurePattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^\\[INFO\\] (BUILD|FATAL) (ERROR|FAILURE)"));
        RegexPatternConfiguration errorPattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^\\[ERROR\\]"));
        errorPattern.getExclusions().addAll(suppressedErrors);

        RegexPostProcessorConfiguration mavenErrorsPP = new RegexPostProcessorConfiguration(MAVEN_ERRORS_PROCESSOR_NAME);
        mavenErrorsPP.getPatterns().add(failurePattern);
        mavenErrorsPP.getPatterns().add(errorPattern);

        RegexPatternConfiguration warningPattern = new RegexPatternConfiguration(Feature.Level.WARNING, Pattern.compile("^\\[WARNING\\]"));
        warningPattern.getExclusions().addAll(suppressedWarnings);
        mavenErrorsPP.getPatterns().add(warningPattern);

        mavenErrorsPP.setFailOnError(false);
        mavenErrorsPP.setLeadingContext(getLeadingContext());
        mavenErrorsPP.setTrailingContext(getTrailingContext());
        group.getProcessors().put(MAVEN_ERRORS_PROCESSOR_NAME, mavenErrorsPP);

        return group;
    }

    public int getLeadingContext()
    {
        return leadingContext;
    }

    public void setLeadingContext(int leadingContext)
    {
        this.leadingContext = leadingContext;
    }

    public int getTrailingContext()
    {
        return trailingContext;
    }

    public void setTrailingContext(int trailingContext)
    {
        this.trailingContext = trailingContext;
    }

    public List<String> getSuppressedWarnings()
    {
        return suppressedWarnings;
    }

    public void setSuppressedWarnings(List<String> suppressedWarnings)
    {
        this.suppressedWarnings = suppressedWarnings;
    }

    public List<String> getSuppressedErrors()
    {
        return suppressedErrors;
    }

    public void setSuppressedErrors(List<String> suppressedErrors)
    {
        this.suppressedErrors = suppressedErrors;
    }

    public void validate(ValidationContext context)
    {
        validateSuppressions(context, suppressedErrors, "suppressedErrors");
        validateSuppressions(context, suppressedWarnings, "suppressedWarnings");
    }

    private void validateSuppressions(ValidationContext context, List<String> suppressions, String field)
    {
        for (String expression: suppressions)
        {
            try
            {
                Pattern.compile(expression);
            }
            catch (PatternSyntaxException e)
            {
                context.addFieldError(field, "Invalid expression '" + expression + "': " + e.getMessage());
            }
        }
    }
}
