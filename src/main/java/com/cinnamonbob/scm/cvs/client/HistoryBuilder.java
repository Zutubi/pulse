package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.*;

/**
 * 
 *
 */
public class HistoryBuilder implements Builder
{
    private List<HistoryInfo> infos = new LinkedList<HistoryInfo>();

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (!isErrorMessage)
        {
            infos.add(parse(line));
        }
    }

    public void parseEnhancedMessage(String key, Object value)
    {
        // noop.
    }

    public void outputDone()
    {
        // finalise 
    }

    public List<HistoryInfo> getHistoryInfo()
    {
        return Collections.unmodifiableList(infos);
    }

    private HistoryInfo parse(String data)
    {
        return new HistoryInfo(data);
    }
}
