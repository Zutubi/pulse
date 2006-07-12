package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;

import java.util.regex.Pattern;

/**
 * <class-comment/>
 */
public class XCodePostProcessor extends PostProcessorGroup
{
    private String[] errorRegexs = new String[]
    {
            ".*error:.*",
            ".*Assertion failure.*",
            ".*No such file or directory.*",
            ".*Undefined symbols.*",
            ".*Uncaught exception:.*"
    };

    private String[] warningRegexs = new String[]
    {
            ".*warning:.*"
    };

    public XCodePostProcessor()
    {
        this(null);
    }

    public XCodePostProcessor(String name)
    {
        setName(name);

        // Regex for error patterns from xcode itself
        RegexPostProcessor xcode = new RegexPostProcessor();

        for (String errorRegex : errorRegexs)
        {
            RegexPattern pattern = xcode.createPattern();
            pattern.setPattern(Pattern.compile(errorRegex));
            pattern.setCategory(Feature.Level.ERROR);
        }

        for (String warnRegex : warningRegexs)
        {
            RegexPattern pattern = xcode.createPattern();
            pattern.setPattern(Pattern.compile(warnRegex));
            pattern.setCategory(Feature.Level.WARNING);
        }

        xcode.setLeadingContext(1);
        xcode.setTrailingContext(6);
        add(xcode);
    }
}
