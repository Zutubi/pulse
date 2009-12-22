package com.zutubi.pulse.core.postprocessors.visualstudio;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for Visual Studio tool
 * output.  Captures error and warning messages.
 */
@SymbolicName("zutubi.visualStudioPostProcessorConfig")
public class VisualStudioPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public VisualStudioPostProcessorConfiguration()
    {
        // CSC : error CS2001: Source file 'Test.cs' could not be found
        // CSC : fatal error CS2008: No inputs specified
        // Test.cs(5,16): error CS0103: The name 'i' does not exist in the current context
        //
        // The format appears to be:
        // <tool/file> : [fatal] error <code>: <message>
        // The file name and location are given if the message can be pinned to
        // a file, otherwise the tool name is used as a prefix.
        addErrorRegexes("^\\w.*:( fatal)? error [A-Z]*[0-9]*:");
        addWarningRegexes("^\\w.*: warning [A-Z]*[0-9]*:");

        setFailOnError(false);
        setLeadingContext(2);
        setTrailingContext(3);
    }
}
