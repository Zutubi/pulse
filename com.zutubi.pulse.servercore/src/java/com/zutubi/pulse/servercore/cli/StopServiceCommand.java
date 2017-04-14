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

package com.zutubi.pulse.servercore.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Internal command used to stop when running under Java Service Wrapper.
 */
public class StopServiceCommand extends AdminCommand
{
    public String getHelp()
    {
        // Internal command
        return null;
    }

    public String getDetailedHelp()
    {
        return null;
    }

    public List<String> getUsages()
    {
        return null;
    }

    public List<String> getAliases()
    {
        return null;
    }

    public Map<String, String> getOptions()
    {
        return null;
    }

    public boolean isDefault()
    {
        return false;
    }

    public int doExecute(String[] argv) throws XmlRpcException, IOException, ParseException
    {
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(getSharedOptions(), argv, false);
        super.processSharedOptions(commandLine);
        xmlRpcClient.execute("RemoteApi.stopService", new Vector<Object>(Arrays.asList(adminToken)));
        return 0;
    }
}
