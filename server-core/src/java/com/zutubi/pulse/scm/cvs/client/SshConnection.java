package com.zutubi.pulse.scm.cvs.client;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.ConnectionModifier;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

import java.io.IOException;

/**
 * Implementation of an SSH Connection to a remote cvs server.
 */
public class SshConnection extends ExtConnection
{
    public static final int DEFAULT_SSH_PORT = 22;

    private SshClient sshClient;
    private SessionChannelClient channelClient;

    /**
     * @param cvsRoot
     */
    public SshConnection(CVSRoot cvsRoot)
    {
        super(cvsRoot);

        if (getPort() == 0)
            setPort(DEFAULT_SSH_PORT);
    }

    /**
     * @throws AuthenticationException
     * @throws CommandAbortedException
     */
    public void open() throws AuthenticationException, CommandAbortedException
    {
        try
        {
            // Connect:
            connect();

            // Authenticate:
            authenticate();

            // Open Channel:
            channelClient = sshClient.openSessionChannel();

            boolean okay = channelClient.executeCommand("cvs server");
            if (!okay)
                throw new CommandAbortedException("Unable to execute cvs server process.", "");

            setInputStream(new LoggedDataInputStream(channelClient.getInputStream()));
            setOutputStream(new LoggedDataOutputStream(channelClient.getOutputStream()));

        } catch (IOException e)
        {
            e.printStackTrace();
            throw new AuthenticationException(e, "");
        }
    }

    /**
     * @throws AuthenticationException
     */
    public void verify() throws AuthenticationException
    {
        try
        {
            try
            {
                connect();
                authenticate();
            }
            finally
            {
                close();
            }
        }
        catch (IOException e)
        {
            throw new AuthenticationException(e, "");
        }
    }

    private void authenticate() throws IOException, AuthenticationException
    {
        PasswordAuthenticationClient pwdAuth = new PasswordAuthenticationClient();
        pwdAuth.setUsername(user);
        pwdAuth.setPassword(password);

        int result = sshClient.authenticate(pwdAuth);
        switch (result)
        {
            case AuthenticationProtocolState.COMPLETE:
                // all is good.
                break;
            default:
                throw new AuthenticationException("Authentication failed, " +
                        "authentication protocol state: " + result, "");
        }
    }

    private void connect() throws IOException
    {
        SshConnectionProperties properties = new SshConnectionProperties();

        properties.setHost(host);
        properties.setPort(port);

        sshClient = new SshClient();
        sshClient.connect(properties, new IgnoreHostKeyVerification());
    }

    /**
     * @see org.netbeans.lib.cvsclient.connection.Connection#close()
     */
    public void close() throws IOException
    {
        try
        {
            if (channelClient != null)
                channelClient.close();
        }
        finally
        {
            if (sshClient != null && sshClient.isConnected())
                sshClient.disconnect();

            sshClient = null;
            channelClient = null;
        }
    }

    /**
     * @see org.netbeans.lib.cvsclient.connection.Connection#isOpen()
     */
    public boolean isOpen()
    {
        return sshClient != null && sshClient.isAuthenticated();
    }


    /**
     * @see org.netbeans.lib.cvsclient.connection.Connection#modifyInputStream(org.netbeans.lib.cvsclient.connection.ConnectionModifier)
     */
    public void modifyInputStream(ConnectionModifier connectionModifier) throws IOException
    {
        connectionModifier.modifyInputStream(getInputStream());
    }

    /**
     * @see org.netbeans.lib.cvsclient.connection.Connection#modifyOutputStream(org.netbeans.lib.cvsclient.connection.ConnectionModifier)
     */
    public void modifyOutputStream(ConnectionModifier connectionModifier) throws IOException
    {
        connectionModifier.modifyOutputStream(getOutputStream());
    }

    public int getPort()
    {
        return port;
    }

}
