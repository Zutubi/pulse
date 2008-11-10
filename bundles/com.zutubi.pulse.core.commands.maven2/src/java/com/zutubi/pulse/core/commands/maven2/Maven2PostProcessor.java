package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.ExpressionElement;
import com.zutubi.pulse.core.RegexPattern;
import com.zutubi.pulse.core.commands.core.JUnitSummaryPostProcessor;
import com.zutubi.pulse.core.commands.core.PostProcessorGroup;
import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.util.SystemUtils;

import java.util.regex.Pattern;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    private RegexPostProcessor failurePP;
    private RegexPostProcessor featurePP;
    private RegexPattern warningPattern;
    private RegexPattern errorPattern;

    public Maven2PostProcessor()
    {
        this(null);
    }

    public Maven2PostProcessor(String name)
    {
        setName(name);

        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        add(new JUnitSummaryPostProcessor());

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

        errorPattern = new RegexPattern(Feature.Level.ERROR, Pattern.compile("^\\[ERROR\\]"));
        errorPattern.addExclusion(failurePattern);
        featurePP = new RegexPostProcessor();
        featurePP.addRegexPattern(errorPattern);

        warningPattern = new RegexPattern(Feature.Level.WARNING, Pattern.compile("^\\[WARNING\\]"));
        featurePP.addRegexPattern(warningPattern);

        featurePP.setFailOnError(false);
        featurePP.setLeadingContext(1);
        featurePP.setTrailingContext(1);
        add(featurePP);

        failurePP = new RegexPostProcessor();
        RegexPattern failureRegex = new RegexPattern(Feature.Level.ERROR, failurePattern);
        failurePP.addRegexPattern(failureRegex);

        if (!SystemUtils.IS_WINDOWS)
        {
            // Prefer the exit code on all systems except windows.
            failurePP.setFailOnError(false);
        }

        failurePP.setLeadingContext(1);
        failurePP.setTrailingContext(6);
        add(failurePP);
    }

    public ExpressionElement createSuppressWarning(String expression)
    {
        ExpressionElement element = new ExpressionElement();
        warningPattern.addExclusion(element);
        return element;
    }

    public ExpressionElement createSuppressError(String expression)
    {
        ExpressionElement element = new ExpressionElement();
        errorPattern.addExclusion(element);
        return element;
    }

    public void addSuppressWarning(ExpressionElement element)
    {
        warningPattern.addExclusion(element);
    }

    public void addSuppressError(ExpressionElement element)
    {
        errorPattern.addExclusion(element);
    }

    public void setFailOnError(boolean fail)
    {
        featurePP.setFailOnError(fail);
        failurePP.setFailOnError(fail);
    }

    public void setFailOnWarning(boolean fail)
    {
        featurePP.setFailOnWarning(fail);
        failurePP.setFailOnWarning(fail);
    }

    public void setLeadingContext(int lines)
    {
        featurePP.setLeadingContext(lines);
        failurePP.setLeadingContext(lines);
    }

    public void setTrailingContext(int lines)
    {
        featurePP.setTrailingContext(lines);
        failurePP.setTrailingContext(lines);
    }

    RegexPattern getWarningPattern()
    {
        return warningPattern;
    }

    RegexPattern getErrorPattern()
    {
        return errorPattern;
    }
}
