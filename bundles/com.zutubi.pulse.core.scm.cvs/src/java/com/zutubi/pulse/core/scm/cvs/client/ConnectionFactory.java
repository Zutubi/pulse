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

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionFactory.java

package com.zutubi.pulse.core.scm.cvs.client;

import com.zutubi.pulse.core.scm.cvs.client.connection.SshConnection;
import com.zutubi.util.StringUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.LocalConnection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;

public class ConnectionFactory
{
    public static Connection getConnection(CVSRoot cvsRoot, String password)
    {
        String method = cvsRoot.getMethod();
        if (CVSRoot.METHOD_EXT.equals(method))
        {
            SshConnection sshConnection = new SshConnection(cvsRoot);
            if (StringUtils.stringSet(password))
            {
                sshConnection.setPassword(password);
            }
            return sshConnection;
        }
        else if (CVSRoot.METHOD_PSERVER.equals(method))
        {
            PServerConnection pServerConnection = new PServerConnection(cvsRoot);
            if (StringUtils.stringSet(password))
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
