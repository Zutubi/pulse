// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HistoryBuilder.java

package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.scm.ScmException;
import org.netbeans.lib.cvsclient.command.Builder;

import java.util.*;

// Referenced classes of package com.zutubi.pulse.scm.cvs.client.commands:
//            HistoryInfo

public class HistoryBuilder implements Builder
{
    public HistoryBuilder()
    {
        infos = new LinkedList();
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (!isErrorMessage && !line.equals("No records selected."))
            try
            {
                HistoryInfo info = parse(line);
                infos.add(info);
            }
            catch (ScmException e)
            {
                e.printStackTrace();
            }
    }

    public void parseEnhancedMessage(String s, Object obj)
    {
    }

    public void outputDone()
    {
    }

    public List getHistoryInfo()
    {
        return Collections.unmodifiableList(infos);
    }

    private HistoryInfo parse(String data)
            throws ScmException
    {
        HistoryInfo info = new HistoryInfo();
        StringTokenizer tokenizer = new StringTokenizer(data, " ", false);
        List<String> tokens = new ArrayList<String>();
        while (tokenizer.hasMoreTokens())
        {
            tokens.add(tokenizer.nextToken());
        }

        if (tokens.size() < 8)
        {
            throw new ScmException("Unable to extract history info from data: " + data);
        }

        info.code = (tokens.get(0));
        info.date = (tokens.get(1)); // date
        info.time = (tokens.get(2)); // time
        info.timezone = (tokens.get(3)); // timezone
        info.user = (tokens.get(4));

        if (info.isUpdate() || info.isCommit())
        {
            info.revision = (tokens.get(5)); // file version
            info.file = (tokens.get(6)); // file name
            info.pathInRepository = (tokens.get(7)); // path in repository
            tokens.get(8); // ==
            info.workingpath = (tokens.get(9)); // working path
        }
        else
        {
            info.file = (tokens.get(5)); // file
            info.pathInRepository = (tokens.get(6).substring(1, tokens.get(6).length() - 1)); // =path in repository=
            info.workingpath = (tokens.get(7)); // working path
        }
        return info;
    }

    private List infos;
    static final String NO_RECORDS = "No records selected.";
}
