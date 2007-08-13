package com.zutubi.pulse.core.scm.cvs.client.commands;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

public class StatusListener extends CVSAdapter
{
    public StatusListener()
    {
        info = new LinkedList<StatusInformation>();
    }

    public void fileInfoGenerated(FileInfoEvent e)
    {
        info.add((StatusInformation)e.getInfoContainer());
    }

    public List<StatusInformation> getInfo()
    {
        return info;
    }

    private List<StatusInformation> info;
}
