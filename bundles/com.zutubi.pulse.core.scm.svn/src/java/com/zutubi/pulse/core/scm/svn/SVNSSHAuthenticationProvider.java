package com.zutubi.pulse.core.scm.svn;

import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSHAuthentication;

import java.io.File;

/**
 */
public class SVNSSHAuthenticationProvider implements ISVNAuthenticationProvider
{
    private final String username;
    private final String privateKeyFile;
    private final String passphrase;

    public SVNSSHAuthenticationProvider(String username, String privateKeyFile, String passphrase)
    {
        this.username = username;
        this.privateKeyFile = privateKeyFile;
        this.passphrase = passphrase;
    }

    public SVNAuthentication requestClientAuthentication(String kind, SVNURL url, String realm, SVNErrorMessage errorMessage, SVNAuthentication previousAuth, boolean authMayBeStored)
    {
        return new SVNSSHAuthentication(username, new File(privateKeyFile), passphrase, 22, false);
    }

    public int acceptServerAuthentication(SVNURL url, String realm, Object certificate, boolean resultMayBeStored)
    {
        return ACCEPTED;
    }
}
