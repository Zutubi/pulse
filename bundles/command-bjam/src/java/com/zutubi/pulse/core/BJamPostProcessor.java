package com.zutubi.pulse.core;

/**
 * A post processor that looks for error messages from Boost Jam (bjam).
 */
public class BJamPostProcessor extends RegexPostProcessor
{
    public BJamPostProcessor()
    {
        addErrorRegexs("^error:",
                       "^rule [a-zA-Z0-9_-]+ unknown",
                       "^\\.\\.\\.failed",
                       "^\\*\\*\\* argument error",
                       "^don't know how to make",
                       "^syntax error");

        addWarningRegexs("^warning:");

        setFailOnError(false);
        setLeadingContext(1);
        setTrailingContext(3);
    }

}
