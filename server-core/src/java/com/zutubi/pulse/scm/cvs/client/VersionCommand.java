/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.ClientServices;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.request.CommandRequest;

/**
 * Execute the cvs version command to retrieve the version of the CVS version.
 * <p/>
 * The version command has no options.
 */
public class VersionCommand extends BasicCommand
{
    public void execute(ClientServices client, EventManager em) throws CommandException, AuthenticationException
    {
        client.ensureConnection();
        super.execute(client, em);

        // if we need to add options to the command, they would be added here...

        requests.add(CommandRequest.VERSION);
        try
        {
            client.processRequests(requests);
            requests.clear();
        }
        catch (CommandException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
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

    /**
     * The Version command does not support any options.
     *
     * @param opt
     * @param optArg
     * @return false since this command does not support any options.
     */
    public boolean setCVSCommand(char opt, String optArg)
    {
        // no options to set.
        return false;
    }

    /**
     * Reset this CVS command instance.
     */
    public void resetCVSCommand()
    {
        if (builder != null)
        {
            ((VersionBuilder) builder).reset();
        }
    }

    /**
     * Get the list of options that are allowed for this command. In the case of
     * this command, we return an empty string since the version command does not
     * support any options.
     *
     * @return a single of letters that represent the allowable command options.
     */
    public String getOptString()
    {
        return "";
    }

    public Builder createBuilder(EventManager eventMan)
    {
        return new VersionBuilder();
    }

    public String getVersion()
    {
        return ((VersionBuilder) builder).getServerVersion();
    }
}
