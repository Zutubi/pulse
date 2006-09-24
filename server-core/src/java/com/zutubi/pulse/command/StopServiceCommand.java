package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Vector;
import java.util.Arrays;

/**
 * Internal command used to stop when running under Java Service Wrapper.
 */
public class StopServiceCommand extends AdminCommand
{
    public void parse(String... argv) throws ParseException
    {
    }

    public String getHelp()
    {
        // Internal command
        return null;
    }

    public int doExecute() throws XmlRpcException, IOException
    {
        xmlRpcClient.execute("RemoteApi.stopService", new Vector<Object>(Arrays.asList(new Object[]{ adminToken })));
        return 0;
    }
}
