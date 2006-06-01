package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;

import java.util.regex.Pattern;

/**
 * A post processor that looks for error messages from GNU-compatible make
 * programs.
 */
public class MakePostProcessor extends RegexPostProcessor implements PostProcessor
{
    public MakePostProcessor()
    {
        RegexPattern pattern = createPattern();
        pattern.setCategory(Feature.Level.ERROR);
        pattern.setPattern(Pattern.compile("^make(\\[[0-9]+\\])?: \\*\\*\\*"));
        setFailOnError(false);
        setLeadingContext(3);
        setTrailingContext(3);
    }
}
