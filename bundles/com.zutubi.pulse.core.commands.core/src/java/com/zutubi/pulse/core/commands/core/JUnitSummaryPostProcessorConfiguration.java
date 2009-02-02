package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * A post-processor for JUnit summaries.  These are simple status lines
 * printed as JUnit is run.
 */
@SymbolicName("zutubi.junitSummaryPostProcessorConfig")
public class JUnitSummaryPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public JUnitSummaryPostProcessorConfiguration()
    {
        addErrorRegexes("Tests run: .*(Errors|Failures): [1-9]+");

        setFailOnError(false);
        setLeadingContext(1);
    }

    public JUnitSummaryPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
