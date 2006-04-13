/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class LogCommandListener extends CVSAdapter
{
    private List<LogInformation> infos;

    public LogCommandListener()
    {
        infos = new LinkedList<LogInformation>();
    }

    public LogCommandListener(List<LogInformation> infos)
    {
        this.infos = infos;
    }

    public void fileInfoGenerated(FileInfoEvent e)
    {
        LogInformation info = (LogInformation) e.getInfoContainer();
        infos.add(info);
    }

    public List<LogInformation> getLogInfo()
    {
        return this.infos;
    }
}
