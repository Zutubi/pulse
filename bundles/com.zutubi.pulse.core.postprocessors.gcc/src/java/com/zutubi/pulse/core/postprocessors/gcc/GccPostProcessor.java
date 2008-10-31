package com.zutubi.pulse.core.postprocessors.gcc;

import com.zutubi.pulse.core.commands.core.RegexPostProcessor;

/**
 * A post-processor for gcc (and related compiler) output.  Captures error
 * and warning messages.
 */
public class GccPostProcessor extends RegexPostProcessor
{
    public GccPostProcessor()
    {
        addErrorRegexs("^.+:[0-9]+: error:");
        addWarningRegexs("^.+:[0-9]+: warning:");

        setFailOnError(false);
        setLeadingContext(2);
        setTrailingContext(3);
    }
}
