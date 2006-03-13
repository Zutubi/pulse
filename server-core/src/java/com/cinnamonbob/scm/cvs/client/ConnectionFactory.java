package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.LocalConnection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import com.opensymphony.util.TextUtils;

/**
 * 
 *
 */
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
            return new PServerConnection(cvsRoot);
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
