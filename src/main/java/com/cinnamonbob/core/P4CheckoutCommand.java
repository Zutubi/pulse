package com.cinnamonbob.core;

import com.cinnamonbob.core.scm.P4Server;
import com.cinnamonbob.core.scm.SCMException;
import com.cinnamonbob.core.scm.SCMServer;

import java.io.File;

/**
 * A command for checking out code from a Subversion repository.
 * 
 * @author jsankey
 */
public class P4CheckoutCommand extends SCMCheckoutCommand
{
    private String port;
    private String user;
    private String password;
    private String client;
    private File   path;
        
    public P4CheckoutCommand(CommandCommon common) 
    {
        super(common);
    }

    @Override
    protected File getPath()
    {
        return path;
    }

    @Override
    protected SCMServer createServer() throws SCMException
    {
        return new P4Server(port, user, password, client);
    }
    
    @Override
    protected void destroyServer(SCMServer server)
    {
        // Nothing to do
    }
    
    public void setPort(String port)
    {
        this.port = port;
    }
    
    public void setUser(String user)
    {
        this.user = user;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public void setClient(String client)
    {
        this.client = client;
    }
    public void setPath(File path)
    {
        this.path = path;
    }
}
