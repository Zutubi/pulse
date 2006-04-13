/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.scm.SCMException;
import org.netbeans.lib.cvsclient.command.Builder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class HistoryBuilder implements Builder
{
    private List<HistoryInfo> infos = new LinkedList<HistoryInfo>();

    static final String NO_RECORDS = "No records selected.";

    private String module;

    public void setModule(String module)
    {
        this.module = module;
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (!isErrorMessage && !line.equals(NO_RECORDS))
        {
            try
            {
                HistoryInfo info = parse(line);
                if (matchesRequestedModule(info))
                {
                    infos.add(info);
                }
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

    private boolean matchesRequestedModule(HistoryInfo info)
    {
        if (module == null)
        {
            return true;
        }
        return info.getPathInRepository().startsWith(module);
    }
}
