package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.core.PostProcessorGroup;
import com.zutubi.pulse.core.commands.core.RegexPostProcessor;
import com.zutubi.pulse.core.postprocessors.PostProcessor;

/**
 * <class-comment/>
 */
public class XCodePostProcessor extends PostProcessorGroup
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

    public XCodePostProcessor()
    {
        this(null);
    }

    public XCodePostProcessor(String name)
    {
        setName(name);

        // Regex for error patterns from xcode itself
        RegexPostProcessor xcode = new RegexPostProcessor();

        xcode.addErrorRegexs(errorRegexs);
        xcode.addWarningRegexs(warningRegexs);

        xcode.setLeadingContext(1);
        xcode.setTrailingContext(3);
        add(xcode);
    }

    public void setJoinOverlapping(boolean b)
    {
        for(PostProcessor child: getProcessors())
        {
            if(child instanceof RegexPostProcessor)
            {
                ((RegexPostProcessor)child).setJoinOverlapping(b);
            }
        }
    }

    public void setLeadingContext(int leadingContext)
    {
        for(PostProcessor child: getProcessors())
        {
            if(child instanceof RegexPostProcessor)
            {
                ((RegexPostProcessor)child).setLeadingContext(leadingContext);
            }
        }
    }

    public void setTrailingContext(int trailingContext)
    {
        for(PostProcessor child: getProcessors())
        {
            if(child instanceof RegexPostProcessor)
            {
                ((RegexPostProcessor)child).setTrailingContext(trailingContext);
            }
        }
    }
}
