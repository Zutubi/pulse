package com.zutubi.pulse.core;

import com.zutubi.pulse.util.SystemUtils;
import com.zutubi.pulse.core.model.Feature;

import java.util.regex.Pattern;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    private static final String[] warningRegexps = new String[]{
            "^\\[WARNING\\]"
    };
    private RegexPostProcessor failurePP;
    private RegexPostProcessor featurePP;

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

        Pattern failurePattern = Pattern.compile("^\\[ERROR\\] BUILD (ERROR|FAILURE)");

        featurePP = new RegexPostProcessor();
        RegexPattern errorRegex = new RegexPattern(Feature.Level.ERROR, Pattern.compile("^\\[ERROR\\]"));
        errorRegex.addExclusion(failurePattern);
        featurePP.addRegexPattern(errorRegex);
        featurePP.addWarningRegexs(warningRegexps);

        featurePP.setFailOnError(false);
        featurePP.setLeadingContext(1);
        featurePP.setTrailingContext(1);
        add(featurePP);

        failurePP = new RegexPostProcessor();
        RegexPattern failureRegex = new RegexPattern(Feature.Level.ERROR, failurePattern);
        failurePP.addRegexPattern(failureRegex);

        if (!SystemUtils.isWindows())
        {
            // Prefer the exit code on all systems except windows.
            failurePP.setFailOnError(false);
        }

        failurePP.setLeadingContext(1);
        failurePP.setTrailingContext(6);
        add(failurePP);
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
}
