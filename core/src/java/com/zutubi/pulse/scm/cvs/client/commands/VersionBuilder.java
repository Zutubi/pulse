// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VersionBuilder.java

package com.zutubi.pulse.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.command.Builder;

public class VersionBuilder
        implements Builder
{

    public VersionBuilder()
    {
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (isErrorMessage)
        {
            return;
        }
        else
        {
            serverVersion = line;
            return;
        }
    }

    public void parseEnhancedMessage(String s, Object obj)
    {
    }

    public void outputDone()
    {
    }

    public String getServerVersion()
    {
        return serverVersion;
    }

    public void reset()
    {
        serverVersion = null;
    }

    private String serverVersion;
}
