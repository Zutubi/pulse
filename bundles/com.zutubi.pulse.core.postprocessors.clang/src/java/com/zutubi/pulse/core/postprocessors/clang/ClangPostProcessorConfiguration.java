package com.zutubi.pulse.core.postprocessors.clang;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for clang (and related
 * tool) output.  Captures error and warning messages.
 */
@SymbolicName("zutubi.clangPostProcessorConfig")
public class ClangPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public ClangPostProcessorConfiguration()
    {
        addErrorRegexes(
                "^.+:[0-9]+:([0-9]+:)?( fatal)? error:",
                "^\\w+:( fatal)? error:",
                "^Undefined symbols for architecture",
                "^ld: symbol\\(s\\) not found for architecture"
        );
        addWarningRegexes(
                "^.+:[0-9]+:([0-9]+:)? warning:",
                "^\\w+: warning:"
        );

        setFailOnError(false);
        setLeadingContext(0);
        setTrailingContext(3);
    }
}
