// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionFactory.java

package com.zutubi.pulse.scm.cvs.client;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.scm.cvs.client.connection.SshConnection;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.*;

public class ConnectionFactory
{
    public static Connection getConnection(CVSRoot cvsRoot, String password)
    {
        String method = cvsRoot.getMethod();
        if (CVSRoot.METHOD_EXT.equals(method))
        {
            SshConnection sshConnection = new SshConnection(cvsRoot);
            if (TextUtils.stringSet(password))
            {
                sshConnection.setPassword(password);
            }
            return sshConnection;
        }
        else if (CVSRoot.METHOD_PSERVER.equals(method))
        {
            PServerConnection pServerConnection = new PServerConnection(cvsRoot);
            if (TextUtils.stringSet(password))
            {
                String encodedPassword = StandardScrambler.getInstance().scramble(password);
                pServerConnection.setEncodedPassword(encodedPassword);
            }
            return pServerConnection;
        }
        else if (CVSRoot.METHOD_LOCAL.equals(method) || method == null)
        {
            LocalConnection c = new LocalConnection();
            c.setRepository(cvsRoot.getRepository());
            return c;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported connection method: " + method);
        }
    }
}
