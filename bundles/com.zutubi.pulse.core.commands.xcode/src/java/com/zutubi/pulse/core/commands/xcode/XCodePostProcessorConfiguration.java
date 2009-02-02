package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.xcodePostProcessorConfig")
public class XCodePostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    private String[] errorRegexs = new String[]
    {
            "[\\d]+: error:",
            "Assertion failure",
            "No such file or directory",
            "Undefined symbols",
            "Uncaught exception:"
    };

    private String[] warningRegexs = new String[]
    {
            "warning:"
    };

    public XCodePostProcessorConfiguration()
    {
        addErrorRegexes(errorRegexs);
        addWarningRegexes(warningRegexs);

        setLeadingContext(1);
        setTrailingContext(3);
    }

    public XCodePostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}
