package com.cinnamonbob.core2.config;

import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.SVNServer;

import java.io.File;

/**
 * A command for checking out code from a Subversion repository.
 * 
 * @author jsankey
 */
public class SVNCheckoutCommand extends SCMCheckoutCommand
{
    private String user;
    private String password;
    private String keyFile;
    private String passphrase;
    private String url;
    private File   path;
    
    public SVNCheckoutCommand()
    {
    }    
    
    @Override
    protected File getPath()
    {
        return path;
    }    
    
    @Override
    protected SCMServer createServer() throws SCMException
    {
        if(keyFile == null)
        {
            return new SVNServer(url, user, password);
        }
        else
        {
            if(passphrase == null)
            {
                return new SVNServer(url, user, password, keyFile);
            }
            else
            {
                return new SVNServer(url, user, password, keyFile, passphrase);                
            }
        }
    }
    
    @Override
    protected void destroyServer(SCMServer server)
    {
        // Nothing to do
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setKeyFile(String keyFile)
    {
        this.keyFile = keyFile;
    }

    public void setPassphrase(String passphrase)
    {
        this.passphrase = passphrase;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setPath(File path)
    {
        this.path = path;
    }
}
