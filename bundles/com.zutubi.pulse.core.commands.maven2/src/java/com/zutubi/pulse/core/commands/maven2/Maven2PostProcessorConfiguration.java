package com.zutubi.pulse.core.commands.maven2;

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
import com.zutubi.util.SystemUtils;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Min;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Configuration for instances of {@link Maven2PostProcessor}.
 */
@SymbolicName("zutubi.maven2PostProcessorConfig")
@Form(fieldOrder = {"name", "leadingContext", "trailingContext", "suppressedWarnings", "suppressedErrors"})
public class Maven2PostProcessorConfiguration extends AbstractNamedConfiguration implements PostProcessorConfiguration, Validateable
{
    private static final String JUNIT_PROCESSOR_NAME = "junit.summary";
    private static final String MAVEN_ERRORS_PROCESSOR_NAME = "maven.errors";
    private static final String BUILD_FAILURE_PROCESSOR_NAME = "build.failure";

    @Min(0) @Wizard.Ignore
    private int leadingContext = 1;
    @Min(0) @Wizard.Ignore
    private int trailingContext = 6;
    @Addable(value = "suppress-error", attribute = "expression") @StringList @Wizard.Ignore
    private List<String> suppressedWarnings = new LinkedList<String>();
    @Addable(value = "suppress-warning", attribute = "expression") @StringList @Wizard.Ignore
    private List<String> suppressedErrors = new LinkedList<String>();

    public Maven2PostProcessorConfiguration()
    {
    }

    public Maven2PostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }

    public Class<? extends PostProcessor> processorType()
    {
        return Maven2PostProcessor.class;
    }

    public PostProcessorGroupConfiguration asGroup()
    {
        PostProcessorGroupConfiguration group = new PostProcessorGroupConfiguration();

        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        group.getProcessors().put(JUNIT_PROCESSOR_NAME, new JUnitSummaryPostProcessorConfiguration(JUNIT_PROCESSOR_NAME));

        // Life is a little complicated.  It is possible to get [ERROR]
        // reports in a successful Maven build (CIB-616), so we don't just
        // rely on this to detect failure.  Rather, the specific:
        //
        // [ERROR] BUILD FAILURE
        //
        // is used.  We need to use a separate regex.pp for this to allow
        // failOnError to be set on Windows for just this one pattern.  We
        // also need to exclude it from the more generic [ERROR] pattern
        // that is still used to pick up error features.

        Pattern failurePattern = Pattern.compile("^\\[ERROR\\] (BUILD|FATAL) (ERROR|FAILURE)");

        RegexPatternConfiguration errorPattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^\\[ERROR\\]"));
        errorPattern.getExclusions().add(failurePattern.pattern());
        errorPattern.getExclusions().addAll(suppressedErrors);

        RegexPostProcessorConfiguration mavenErrorsPP = new RegexPostProcessorConfiguration(MAVEN_ERRORS_PROCESSOR_NAME);
        mavenErrorsPP.getPatterns().add(errorPattern);

        RegexPatternConfiguration warningPattern = new RegexPatternConfiguration(Feature.Level.WARNING, Pattern.compile("^\\[WARNING\\]"));
        warningPattern.getExclusions().addAll(suppressedWarnings);
        mavenErrorsPP.getPatterns().add(warningPattern);

        mavenErrorsPP.setFailOnError(false);
        mavenErrorsPP.setLeadingContext(getLeadingContext());
        mavenErrorsPP.setTrailingContext(getTrailingContext());
        group.getProcessors().put(MAVEN_ERRORS_PROCESSOR_NAME, mavenErrorsPP);

        RegexPostProcessorConfiguration buildFailurePP = new RegexPostProcessorConfiguration(BUILD_FAILURE_PROCESSOR_NAME);
        RegexPatternConfiguration failureRegex = new RegexPatternConfiguration(Feature.Level.ERROR, failurePattern);
        buildFailurePP.getPatterns().add(failureRegex);

        if (!SystemUtils.IS_WINDOWS)
        {
            // Prefer the exit code on all systems except windows.
            buildFailurePP.setFailOnError(false);
        }

        buildFailurePP.setLeadingContext(getLeadingContext());
        buildFailurePP.setTrailingContext(getTrailingContext());
        group.getProcessors().put(BUILD_FAILURE_PROCESSOR_NAME, buildFailurePP);

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
