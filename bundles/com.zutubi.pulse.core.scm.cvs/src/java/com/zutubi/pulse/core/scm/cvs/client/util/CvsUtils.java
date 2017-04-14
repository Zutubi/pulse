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
// Source File Name:   CvsUtils.java

package com.zutubi.pulse.core.scm.cvs.client.util;

import com.zutubi.util.logging.Logger;
import java.io.IOException;
import org.netbeans.lib.cvsclient.connection.Connection;

public class CvsUtils
{

    public CvsUtils()
    {
    }

    public static void close(Connection c)
    {
        try
        {
            if(c != null && c.isOpen())
                c.close();
        }
        catch(IOException e)
        {
            LOG.info(e);
        }
    }

    private static final Logger LOG = Logger.getLogger(CvsUtils.class);

}
