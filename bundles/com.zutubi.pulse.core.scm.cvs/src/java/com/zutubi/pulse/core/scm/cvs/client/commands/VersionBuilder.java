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
// Source File Name:   VersionBuilder.java

package com.zutubi.pulse.core.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.command.Builder;

public class VersionBuilder
        implements Builder
{

    public VersionBuilder()
    {
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (isErrorMessage)
        {
            return;
        }
        else
        {
            serverVersion = line;
            return;
        }
    }

    public void parseEnhancedMessage(String s, Object obj)
    {
    }

    public void outputDone()
    {
    }

    public String getServerVersion()
    {
        return serverVersion;
    }

    public void reset()
    {
        serverVersion = null;
    }

    private String serverVersion;
}
