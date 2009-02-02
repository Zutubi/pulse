package com.zutubi.pulse.core.postprocessors.gcc;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A post-processor for gcc (and related compiler) output.  Captures error
 * and warning messages.
 */
@SymbolicName("zutubi.gccPostProcessorConfig")
public class GccPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public GccPostProcessorConfiguration()
    {
        addErrorRegexes("^.+:[0-9]+: error:");
        addWarningRegexes("^.+:[0-9]+: warning:");

        setFailOnError(false);
        setLeadingContext(2);
        setTrailingContext(3);
    }
}
