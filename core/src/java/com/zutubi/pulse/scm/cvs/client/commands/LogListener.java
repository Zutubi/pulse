// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LogListener.java

package com.zutubi.pulse.scm.cvs.client.commands;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;

public class LogListener extends CVSAdapter
{

    public LogListener()
    {
        infos = new LinkedList<LogInformation>();
    }

    public LogListener(List<LogInformation> infos)
    {
        this.infos = infos;
    }

    public void fileInfoGenerated(FileInfoEvent e)
    {
        infos.add((LogInformation)e.getInfoContainer());
    }

    public List getLogInfo()
    {
        return infos;
    }

    private List<LogInformation> infos;
}
