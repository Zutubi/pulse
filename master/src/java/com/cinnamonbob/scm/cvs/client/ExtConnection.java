package com.cinnamonbob.scm.cvs.client;

import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.AbstractConnection;

/**
 * The Ext connection method uses a remote shell to connect to the
 * cvs server. Typically, this is RSH or SSH.
 */
public abstract class ExtConnection extends AbstractConnection
{
    protected int port;
    protected String host;
    protected String user;
    protected String password;

    public ExtConnection(CVSRoot cvsRoot)
    {
        if (!CVSRoot.METHOD_EXT.equals(cvsRoot.getMethod()))
        {
            throw new IllegalArgumentException("Can not use '" + cvsRoot.getMethod() + "' with SshConnection.");
        }

        setUserName(cvsRoot.getUserName());
        setHostName(cvsRoot.getHostName());
        setRepository(cvsRoot.getRepository());

        int port = cvsRoot.getPort();
        if (port != 0)
        {
            setPort(cvsRoot.getPort());
        }

        String password = cvsRoot.getPassword();
        if (password != null)
        {
            setPassword(password);
        }
    }

    public void setHostName(String s)
    {
        this.host = s;
    }

    public void setUserName(String s)
    {
        this.user = s;
    }

    public void setPassword(String passwd)
    {
        this.password = passwd;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
