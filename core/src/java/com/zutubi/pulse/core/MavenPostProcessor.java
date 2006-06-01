package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.SystemUtils;

import java.util.regex.Pattern;

/**
 * <class-comment/>
 */
public class MavenPostProcessor extends PostProcessorGroup
{
    public MavenPostProcessor()
    {
        // Add a JUnit summary post processor.  It comes first as the output
        // appears as the tests are run.
        add(new JUnitSummaryPostProcessor());

        // Regex for error patterns from maven itself
        RegexPostProcessor maven = new RegexPostProcessor();

        RegexPattern pattern = maven.createPattern();
        pattern.setPattern(Pattern.compile(".*BUILD FAILED.*"));
        pattern.setCategory(Feature.Level.ERROR);

        pattern = maven.createPattern();
        pattern.setPattern(Pattern.compile("Basedir.*does not exist"));
        pattern.setCategory(Feature.Level.ERROR);

        if (!SystemUtils.isWindows())
        {
            maven.setFailOnError(false);
        }

        maven.setLeadingContext(1);
        maven.setTrailingContext(6);
        add(maven);
    }

    public MavenPostProcessor(String name)
    {
        this();
        setName(name);
    }
}
