package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.ExpressionElementConfiguration;
import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.PostProcessorGroupConfiguration;
import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.PostProcessor;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.SystemUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessorConfiguration extends AbstractNamedConfiguration implements PostProcessorConfiguration
{
    private static final String JUNIT_PROCESSOR_NAME = "junit.summary";
    private static final String MAVEN_ERRORS_PROCESSOR_NAME = "maven.errors";
    private static final String BUILD_FAILURE_PROCESSOR_NAME = "build.failure";

    private int leadingContext = 1;
    private int trailingContext = 6;
    @Addable("suppress-error")
    private List<ExpressionElementConfiguration> suppressedWarnings = new LinkedList<ExpressionElementConfiguration>();
    @Addable("suppress-warning")
    private List<ExpressionElementConfiguration> suppressedErrors = new LinkedList<ExpressionElementConfiguration>();

    public Maven2PostProcessorConfiguration(String name)
    {
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
        // realy on this to detect failure.  Rather, the specific:
        //
        // [ERROR] BUILD FAILURE
        //
        // is used.  We need to use a separate regex.pp for this to allow
        // failOnError to be set on Windows for just this one pattern.  We
        // also need to exclude it from the more generic [ERROR] pattern
        // that is still used to pick up error features.

        Pattern failurePattern = Pattern.compile("^\\[ERROR\\] (BUILD|FATAL) (ERROR|FAILURE)");

        RegexPatternConfiguration errorPattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("^\\[ERROR\\]"));
        errorPattern.getExclusions().add(new ExpressionElementConfiguration(failurePattern.pattern()));
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

    public List<ExpressionElementConfiguration> getSuppressedWarnings()
    {
        return suppressedWarnings;
    }

    public void setSuppressedWarnings(List<ExpressionElementConfiguration> suppressedWarnings)
    {
        this.suppressedWarnings = suppressedWarnings;
    }

    public List<ExpressionElementConfiguration> getSuppressedErrors()
    {
        return suppressedErrors;
    }

    public void setSuppressedErrors(List<ExpressionElementConfiguration> suppressedErrors)
    {
        this.suppressedErrors = suppressedErrors;
    }
}
