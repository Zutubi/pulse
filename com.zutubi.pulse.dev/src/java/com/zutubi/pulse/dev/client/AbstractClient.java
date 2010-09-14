package com.zutubi.pulse.dev.client;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.pulse.dev.config.DevConfigSetup;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcClient;
import com.zutubi.pulse.dev.xmlrpc.PulseXmlRpcException;

import java.net.MalformedURLException;

/**
 * Abstract base for clients that communicate with a Pulse server.
 */
public abstract class AbstractClient<T extends DevConfig>
{
    private static final Messages I18N = Messages.getInstance(AbstractClient.class);
    
    protected T config;
    protected UserInterface ui;
    private String password;
    
    public AbstractClient(T config, UserInterface ui)
    {
        this.config = config;
        this.ui = ui;
    }

    public T getConfig()
    {
        return config;
    }

    public UserInterface getUI()
    {
        return ui;
    }

    protected void ensureServerConfigured() throws ClientException
    {
        ui.debug("Checking pulse server configuration...");
        if (config.getPulseUrl() == null)
        {
            YesNoResponse response = ui.yesNoPrompt(I18N.format("prompt.pulse.server"), false, false, YesNoResponse.YES);
            if (response.isAffirmative())
            {
                DevConfigSetup.setupPulseConfig(ui, config);
            }
            else
            {
                throw new ClientException("Required property '" + DevConfig.PROPERTY_PULSE_URL + "' not specified.");
            }
        }
    }

    protected PulseXmlRpcClient getXmlRpcClient() throws ClientException
    {
        try
        {
            return new PulseXmlRpcClient(config.getPulseUrl(), config.getProxyHost(), config.getProxyPort());
        }
        catch (MalformedURLException e)
        {
            throw new ClientException("Invalid pulse server URL '" + config.getPulseUrl() + "'", e);
        }
    }
    
    protected void checkVersion(PulseXmlRpcClient rpc) throws ClientException
    {
        int ourBuild = Version.getVersion().getBuildNumberAsInt();
        int confirmedBuild = config.getConfirmedVersion();

        ui.debug("Checking pulse server version...");
        try
        {
            int serverBuild = rpc.getVersion();
            // A negative server build indicates a development setup, in which
            // case we assume the user knows what they are doing (hmmm...).
            if (serverBuild >= 0 && serverBuild != ourBuild)
            {
                ui.debug(String.format("Server build (%d) does not match local build (%d)", serverBuild, ourBuild));
                if (serverBuild != confirmedBuild)
                {
                    String serverVersion = Version.buildNumberToVersion(serverBuild);
                    String ourVersion = Version.buildNumberToVersion(ourBuild);
                    String question;

                    if (serverVersion.equals(ourVersion))
                    {
                        question = I18N.format("prompt.build.mismatch", serverBuild, ourBuild);
                    }
                    else
                    {
                        question = I18N.format("prompt.version.mismatch", serverVersion, ourVersion);
                    }

                    YesNoResponse response = ui.yesNoPrompt(question, true, false, YesNoResponse.NO);
                    if (response.isPersistent())
                    {
                        config.setConfirmedVersion(serverBuild);
                    }

                    if (!response.isAffirmative())
                    {
                        throw new UserAbortException();
                    }
                }
            }

            ui.debug("Version accepted.");
        }
        catch (PulseXmlRpcException e)
        {
            throw new ClientException("Unable to get pulse server version: " + e.getMessage(), e);
        }
    }

    protected String login(PulseXmlRpcClient rpc)
    {
        ui.debug("Logging in to pulse: url: " + config.getPulseUrl() + ", user: " + config.getPulseUser());
        String token = rpc.login(config.getPulseUser(), getPassword());
        ui.debug("Login successful.");
        return token;
    }

    protected String getPassword()
    {
        if (password == null)
        {
            password = config.getPulsePassword();
            if (password == null)
            {
                password = ui.passwordPrompt("Pulse password");
                if (password == null)
                {
                    password = "";
                }
            }
        }

        return password;
    }
}
