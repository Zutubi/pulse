package com.zutubi.pulse.core.util.process;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class BufferingLineHandler extends LineHandlerSupport
{
    private List<String> stdout = new LinkedList<String>();
    private List<String> stderr = new LinkedList<String>();

    public void handle(String line, boolean error)
    {
        if(error)
        {
            stderr.add(line);
        }
        else
        {
            stdout.add(line);
        }
    }

    public List<String> getStdout()
    {
        return stdout;
    }

    public List<String> getStderr()
    {
        return stderr;
    }
}
