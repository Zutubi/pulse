package com.zutubi.pulse.core.postprocessors.gcc;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for gcc (and related
 * compiler) output.  Captures error and warning messages.
 */
@SymbolicName("zutubi.gccPostProcessorConfig")
public class GccPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public GccPostProcessorConfiguration()
    {
        addErrorRegexes(
                "^.+:[0-9]+: error:",
                ": undefined reference to",
                "^collect2: ld returned [1-9][0-9]* exit status"
        );
        addWarningRegexes("^.+:[0-9]+: warning:");

        setFailOnError(false);
        setLeadingContext(2);
        setTrailingContext(3);
    }
}
