package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;

import java.util.regex.Pattern;

/**
 * A post processor that looks for error messages from Boost Jam (bjam).
 */
public class BJamPostProcessor extends RegexPostProcessor implements PostProcessor
{
    public BJamPostProcessor()
    {
        initPattern("^rule [a-zA-Z0-9_-]+ unknown", Feature.Level.ERROR);
        initPattern("^\\.\\.\\.failed", Feature.Level.ERROR);
        initPattern("^\\*\\*\\* argument error", Feature.Level.ERROR);
        initPattern("^don't know how to make", Feature.Level.ERROR);
        initPattern("^syntax error", Feature.Level.ERROR);

        initPattern("^warning:", Feature.Level.WARNING);

        setFailOnError(false);
        setLeadingContext(1);
        setTrailingContext(3);
    }

    private void initPattern(String regex, Feature.Level error)
    {
        RegexPattern pattern = createPattern();
        pattern.setCategory(error);
        pattern.setPattern(Pattern.compile(regex));
    }
}
