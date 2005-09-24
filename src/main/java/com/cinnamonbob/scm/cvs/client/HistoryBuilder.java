package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.*;

import com.cinnamonbob.scm.SCMException;

/**
 * 
 *
 */
public class HistoryBuilder implements Builder
{
    private List<HistoryInfo> infos = new LinkedList<HistoryInfo>();

    static final String NO_RECORDS = "No records selected.";

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (!isErrorMessage && !line.equals(NO_RECORDS))
        {
            try
            {
                infos.add(parse(line));
            }
            catch (SCMException e)
            {
                e.printStackTrace();
            }
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

    private HistoryInfo parse(String data) throws SCMException
    {
        return new HistoryInfo(data);
    }
}
