package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A post processor that looks for error messages from GNU-compatible make
 * programs.
 */
@SymbolicName("zutubi.makePostProcessorConfig")
public class MakePostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public MakePostProcessorConfiguration()
    {
        addErrorRegexs("^make(\\[[0-9]+\\])?: \\*\\*\\*");
        setFailOnError(false);
        setLeadingContext(3);
        setTrailingContext(3);
    }
}
