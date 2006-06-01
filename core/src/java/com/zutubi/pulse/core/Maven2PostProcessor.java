package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.SystemUtils;

import java.util.regex.Pattern;

/**
 * A post-processor for maven 2 output.  Attempts to capture features from Maven
 * itself (e.g. "[ERROR] BUILD ERROR") and from commonly-used plugins.
 */
public class Maven2PostProcessor extends PostProcessorGroup
{
    public Maven2PostProcessor()
    {
        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        add(new JUnitSummaryPostProcessor());

        // Regex for error patterns from maven itself
        RegexPostProcessor maven = new RegexPostProcessor();

        RegexPattern pattern = maven.createPattern();
        pattern.setPattern(Pattern.compile("^\\[ERROR\\]"));
        pattern.setCategory(Feature.Level.ERROR);

        pattern = maven.createPattern();
        pattern.setPattern(Pattern.compile("^\\[WARNING\\]"));
        pattern.setCategory(Feature.Level.WARNING);

        if (!SystemUtils.isWindows())
        {
            // By default, prefer the exit code!
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        add(maven);
    }

    public Maven2PostProcessor(String name)
    {
        this();
        setName(name);
    }
}
