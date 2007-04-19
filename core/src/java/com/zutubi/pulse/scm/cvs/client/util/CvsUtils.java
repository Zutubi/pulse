// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CvsUtils.java

package com.zutubi.pulse.scm.cvs.client.util;

import com.zutubi.util.logging.Logger;
import java.io.IOException;
import org.netbeans.lib.cvsclient.connection.Connection;

public class CvsUtils
{

    public CvsUtils()
    {
    }

    public static void close(Connection c)
    {
        try
        {
            if(c != null && c.isOpen())
                c.close();
        }
        catch(IOException e)
        {
            LOG.info(e);
        }
    }

    private static final Logger LOG = Logger.getLogger(CvsUtils.class);

}
