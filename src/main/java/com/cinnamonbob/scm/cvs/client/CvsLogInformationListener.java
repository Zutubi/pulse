package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class CvsLogInformationListener extends CVSAdapter
{
    private List<LogInformation> infos;

    public CvsLogInformationListener()
    {
        infos = new LinkedList<LogInformation>();
    }

    public CvsLogInformationListener(List<LogInformation> infos)
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
