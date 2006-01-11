package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.LocalConnection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;

/**
 * 
 *
 */
public class ConnectionFactory
{
    public static Connection getConnection(String cvsRoot)
    {
        CVSRoot root = CVSRoot.parse(cvsRoot);
        return getConnection(root);
    }

    public static Connection getConnection(CVSRoot cvsRoot)
    {
        String method = cvsRoot.getMethod();
        if (CVSRoot.METHOD_EXT.equals(method))
        {
            //TODO: need to support .cvspasswd file authentication
            if (cvsRoot.getUserName() == null || cvsRoot.getPassword() == null)
            {
                throw new IllegalArgumentException("Authentication details " +
                        "required in the cvsroot to construct SSH connection.");
            }
            return new SshConnection(cvsRoot);
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
