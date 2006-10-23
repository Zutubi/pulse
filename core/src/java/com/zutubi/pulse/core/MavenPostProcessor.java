package com.zutubi.pulse.core;

import com.zutubi.pulse.util.SystemUtils;

/**
 * <class-comment/>
 */
public class MavenPostProcessor extends PostProcessorGroup
{
    private static final String[] errorRegexs = new String[]{
            ".*BUILD FAILED.*",
            "Basedir.*does not exist",
            ".*The build cannot continue because of the following unsatisfied dependencies:.*"
    };

    public MavenPostProcessor()
    {
        this(null);
    }

    public MavenPostProcessor(String name)
    {
        setName(name);

        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        add(new JUnitSummaryPostProcessor());

        // Regex for error patterns from maven itself
        RegexPostProcessor maven = new RegexPostProcessor();
        maven.addErrorRegexs(errorRegexs);

        if (!SystemUtils.IS_WINDOWS)
        {
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        add(maven);
    }
}
