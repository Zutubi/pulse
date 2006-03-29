package com.cinnamonbob.web.ajax;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.Svn;

/**
 * An ajax request to test subversion settings and send a fragment of HTML
 * with results.
 */
public class TestSvnAction extends BaseTestScmAction
{
    private String username;
    private String password;
    private String url;
    private String keyfile;
    private String passphrase;

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setKeyfile(String keyfile)
    {
        this.keyfile = keyfile;
    }

    public void setPassphrase(String passphrase)
    {
        this.passphrase = passphrase;
    }

    public Scm getScm()
    {
        Svn svnConnection = new Svn();
        svnConnection.setUsername(username);
        svnConnection.setUrl(url);
        svnConnection.setPassword(password);
        svnConnection.setKeyfile(keyfile);
        svnConnection.setPassphrase(passphrase);
        return svnConnection;
    }
}
