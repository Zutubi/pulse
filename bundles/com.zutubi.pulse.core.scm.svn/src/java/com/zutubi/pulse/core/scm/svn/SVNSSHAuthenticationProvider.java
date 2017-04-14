/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
