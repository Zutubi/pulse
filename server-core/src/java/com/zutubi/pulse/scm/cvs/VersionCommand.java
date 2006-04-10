package com.cinnamonbob.scm.cvs;

import org.netbeans.lib.cvsclient.ClientServices;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.request.CommandRequest;

import java.lang.reflect.Constructor;

/**
 * Execute the cvs version command to retrieve the version of the CVS version.
 *
 * The version command has no options.
 */
public class VersionCommand extends BasicCommand
{
    public static CommandRequest VERSION;
    static
    {
        try
        {
            // time to create a command request with the 'version\n' string.
            // Unfortunately, the command request has a private constructor, so
            // this is necessary.
            Constructor c = CommandRequest.class.getDeclaredConstructor(String.class);
            c.setAccessible(true);
            VERSION = (CommandRequest) c.newInstance("version\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void execute(ClientServices client, EventManager em) throws CommandException, AuthenticationException
    {
        client.ensureConnection();
        super.execute(client, em);

        // if we need to add options to the command, they would be added here...

        requests.add(VERSION);
        try {
            client.processRequests(requests);
            requests.clear();
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
    }

    /**
     * Don't send status of local files prior to executing command, as it's not
     * needed.
     */
    protected boolean assumeLocalPathWhenUnspecified()
    {
      return false;
    }

    public String getCVSCommand()
    {
        StringBuffer toReturn = new StringBuffer("version ");
        toReturn.append(getCVSArguments());
        return toReturn.toString();
    }

    public String getCVSArguments()
    {
        StringBuffer toReturn = new StringBuffer("");
        return toReturn.toString();
    }

    public boolean setCVSCommand(char opt, String optArg)
    {
        // no options to set.
        return false;
    }

    public void resetCVSCommand()
    {
        // no options to reset.
    }

    public String getOptString()
    {
        return "";
    }

    public Builder createBuilder(EventManager eventMan)
    {
        return new VersionBuilder(eventMan, this);
    }
}
