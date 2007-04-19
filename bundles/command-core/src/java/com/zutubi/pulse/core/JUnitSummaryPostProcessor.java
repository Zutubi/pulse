package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;

import java.util.regex.Pattern;

/**
 * A post-processor for JUnit summaries.  These are simple status lines
 * printed as JUnit is run.
 */
public class JUnitSummaryPostProcessor extends RegexPostProcessor
{
    public JUnitSummaryPostProcessor()
    {
        // Regex for patterns from maven itself
        RegexPattern pattern = createPattern();
        pattern.setPattern(Pattern.compile("Tests run: .*(Errors|Failures): [1-9]+"));
        pattern.setCategory(Feature.Level.ERROR);

        setFailOnError(false);
        setLeadingContext(1);
    }

    public JUnitSummaryPostProcessor(String name)
    {
        this();
        setName(name);
    }
}
