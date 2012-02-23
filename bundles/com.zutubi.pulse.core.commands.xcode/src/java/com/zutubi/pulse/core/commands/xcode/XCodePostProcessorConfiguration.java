package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for extracting common
 * errors and warnings from xcode build output.
 */
@SymbolicName("zutubi.xcodePostProcessorConfig")
public class XCodePostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    private static final String[] ERROR_REGEXES = new String[]
    {
            "[\\d]+: error:",
            "[\\d]+: fatal error:",
            "[\\d]+ errors? generated",
            "Assertion failure",
            "No such file or directory",
            "Undefined symbols",
            "Uncaught exception:",
            "\\[BEROR\\]",
            "BUILD FAILED"
    };

    private static final String[] WARNING_REGEXES = new String[]
    {
            "warning:",
            "\\[WARN\\]"
    };

    public XCodePostProcessorConfiguration()
    {
        addErrorRegexes(ERROR_REGEXES);
        addWarningRegexes(WARNING_REGEXES);

        setLeadingContext(1);
        setTrailingContext(3);
    }

    public XCodePostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
