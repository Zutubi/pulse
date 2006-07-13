package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Daniel Ostermeier
 */
public class SetPasswordCommand extends AdminCommand
{
    private String user;
    private String password;

    /**
     * Set the username identifying the account for which the password will be updated.
     * @param username
     */
    public void setUser(String username)
    {
        this.user = username;
    }

    /**
     * Set the new password that will be set.
     *
     * @param newPassword
     */
    public void setPassword(String newPassword)
    {
        this.password = newPassword;
    }

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
    public void parse(String... argv) throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("user")
                .withArgName("username")
                .hasArg()
                .withDescription("the user whose password is being set")
                .isRequired()
                .create('u'));
        options.addOption(OptionBuilder.withLongOpt("password")
                .withArgName("password")
                .hasArg()
                .withDescription("the new password")
                .isRequired()
                .create('p'));


        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(options, argv, true);

        // process the command.
        setUser(commandLine.getOptionValue('u'));
        setPassword(commandLine.getOptionValue('p'));
    }

    public int doExecute() throws XmlRpcException, IOException
    {
        xmlRpcClient.execute("RemoteApi.setPassword", new Vector<Object>(Arrays.asList(
                new Object[]{adminToken, user, password})));
        return 0;
    }
}

