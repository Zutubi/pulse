package com.zutubi.pulse.servercore.command;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * The ping server command sends a ping request to the server, useful for checking the
 * pulse servers availability.
 */
public class PingServerCommand implements Command
{
    private String baseUrl = "http://localhost:8080";

    /**
     * Required argument.
     *
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public int execute()
    {
        return execute(null);
    }

    public int execute(BootContext context)
    {
        try
        {
            // construct the remote api path.
            StringBuffer remoteApiUrl = new StringBuffer();
            remoteApiUrl.append(baseUrl);
            if (!baseUrl.endsWith("/"))
            {
                remoteApiUrl.append("/");
            }
            remoteApiUrl.append("xmlrpc");

            URL url = new URL(remoteApiUrl.toString());
            XmlRpcClient client = new XmlRpcClient(url);
            client.execute("RemoteApi.ping", new Vector<Object>());
            return 0;
        }
        catch (MalformedURLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 1;
        }
        catch (IOException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return 2;
        }
        catch (XmlRpcException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            return 3;
        }
    }

    public String getHelp()
    {
        return "pings the pulse server at a given url";
    }

    public String getDetailedHelp()
    {
        return "Sends a basic ping to the remote API service of the pulse server at a given\n" +
               "url.  If no URL is specified, the default is http://localhost:8080.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList(new String[] { "<url>" });
    }

    public List<String> getAliases()
    {
        return Arrays.asList(new String[] { "pi" });
    }

    public Map<String, String> getOptions()
    {
        return new HashMap<String, String>(0);
    }

    public boolean isDefault()
    {
        return false;
    }
}
