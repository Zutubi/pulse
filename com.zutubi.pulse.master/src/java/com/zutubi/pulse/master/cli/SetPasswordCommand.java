package com.zutubi.pulse.master.cli;

import com.zutubi.pulse.core.cli.HelpCommand;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.servercore.cli.AdminCommand;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.PathUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * A command to reset a user's password.
 */
public class SetPasswordCommand extends AdminCommand
{
    private static final String ACTION_SET_PASSWORD = "setPassword";
    
    private String user;
    private String password;

    /**
     * Set the username identifying the account for which the password will be updated.
     * @param username the name of the user for whom the password will be reset
     */
    public void setUser(String username)
    {
        this.user = username;
    }

    /**
     * Set the new password that will be set.
     *
     * @param newPassword the new password
     */
    public void setPassword(String newPassword)
    {
        this.password = newPassword;
    }

    public String getHelp()
    {
        return "reset a user's password";
    }

    public String getDetailedHelp()
    {
        return "Used to reset a user's password, typically because the user has forgotten\n" +
               "their current password.";
    }


    public List<String> getUsages()
    {
        return Arrays.asList("<username> <password>");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("passwd", "password", "sp", "setp");
    }

    public int doExecute(String[] argv) throws XmlRpcException, IOException, ParseException
    {
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(getSharedOptions(), argv, false);

        String[] args = commandLine.getArgs();
        if(args.length < 2)
        {
            HelpCommand helpCommand = new HelpCommand();
            helpCommand.showHelp(ACTION_SET_PASSWORD, this);
            return 1;
        }
        // process the command.
        setUser(args[0]);
        setPassword(args[1]);

        Hashtable<String, Object> setPassword = new Hashtable<String, Object>();
        setPassword.put(CompositeType.XML_RPC_SYMBOLIC_NAME, "zutubi.setPasswordConfig");
        setPassword.put("password", password);
        setPassword.put("confirmPassword", password);
        xmlRpcClient.execute("RemoteApi.doConfigActionWithArgument", new Vector<Object>(Arrays.asList(
                adminToken, PathUtils.getPath(MasterConfigurationRegistry.USERS_SCOPE, user), ACTION_SET_PASSWORD, setPassword)));
        return 0;
    }

    public static void main(String argv[])
    {
        SetPasswordCommand command = new SetPasswordCommand();
        try
        {
            command.execute(argv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
