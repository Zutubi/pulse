package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

import java.util.*;

/**
 * 
 *
 */
public class HistoryBuilder implements Builder
{
    private List<HistoryInformation> infos = new LinkedList<HistoryInformation>();

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

    public List<HistoryInformation> getHistoryInfo()
    {
        return Collections.unmodifiableList(infos);
    }

    private HistoryInformation parse(String data)
    {
        StringTokenizer tokenizer = new StringTokenizer(data, " ", false);
        List<String> tokens = new ArrayList<String>();
        while (tokenizer.hasMoreTokens())
        {
            tokens.add(tokenizer.nextToken());
        }

        HistoryInformation info = new HistoryInformation();

        info.setCode(tokens.get(0));
        info.setDate(tokens.get(1)); // date
        info.setTime(tokens.get(2)); // time
        info.setTimezone(tokens.get(3)); // timezone
        info.setUser(tokens.get(4));

        if (info.isUpdate() || info.isCommit())
        {
            info.setRevision(tokens.get(5)); // file version
            info.setFile(tokens.get(6)); // file name
            info.setPathInRepository(tokens.get(7)); // path in repository
            tokens.get(8); // ==
            info.setWorkingpath(tokens.get(9)); // working path
        } else
        {
            info.setFile(tokens.get(5)); // file
            info.setPathInRepository(tokens.get(6).substring(1, tokens.get(6).length() - 1)); // =path in repository=
            info.setWorkingpath(tokens.get(7)); // working path
        }
        return info;
    }
}
