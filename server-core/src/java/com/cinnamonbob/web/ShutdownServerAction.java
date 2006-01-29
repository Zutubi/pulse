package com.cinnamonbob.web;

import com.cinnamonbob.ShutdownService;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 */
public class ShutdownServerAction extends ActionSupport
{
    private boolean force;
    private ConfigurationManager configurationManager;

    public void setForce(boolean force)
    {
        this.force = force;
    }

    public String execute()
    {
        int adminPort = configurationManager.getAppConfig().getAdminPort();
        Socket socket = null;
        String command;

        if (force)
        {
            command = ShutdownService.Command.FORCED_SHUTDOWN;
        }
        else
        {
            command = ShutdownService.Command.SHUTDOWN;
        }

        try
        {
            socket = new Socket("127.0.0.1", adminPort);
            OutputStream out = socket.getOutputStream();
            out.write(command.getBytes());
            out.close();
        }
        catch (IOException e)
        {
            addActionError("Unable to send shutdown request: " + e.getMessage());
        }
        finally
        {
            IOUtils.close(socket);
        }

        if (hasErrors())
        {
            return ERROR;
        }
        else
        {
            return SUCCESS;
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
