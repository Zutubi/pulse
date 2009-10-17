package com.zutubi.pulse.core.commands.nant;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned regular expression post-processor configuration for NAnt output.
 * Attempts to capture features from NAnt itself (e.g. "BUILD FAILED") and from
 * commonly-used tasks (e.g. csc).
 */
@SymbolicName("zutubi.nantPostProcessorConfig")
public class NAntPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public NAntPostProcessorConfiguration()
    {
        addErrorRegexes("^BUILD FAILED", "^Error loading buildfile.", ": error [A-Z0-9]+:");
        addWarningRegexes(": warning [A-Z0-9]+:");

        setLeadingContext(3);
        setTrailingContext(3);
    }

    public NAntPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
