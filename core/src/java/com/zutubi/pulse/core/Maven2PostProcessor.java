package com.zutubi.pulse.core;

import com.zutubi.pulse.util.SystemUtils;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    private static final String[] errorRegexps = new String[]{
            "^\\[ERROR\\]"
    };

    private static final String[] warningRegexps = new String[]{
            "^\\[WARNING\\]"
    };


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

        // Regex for error patterns from maven itself
        RegexPostProcessor maven = new RegexPostProcessor();
        maven.addErrorRegexs(errorRegexps);
        maven.addWarningRegexs(warningRegexps);

        if (!SystemUtils.isWindows())
        {
            // By default, prefer the exit code!
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        add(maven);
    }
}
