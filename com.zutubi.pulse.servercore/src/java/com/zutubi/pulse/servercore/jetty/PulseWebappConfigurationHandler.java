package com.zutubi.pulse.servercore.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.SocketListener;
import org.mortbay.http.NCSARequestLog;
import org.mortbay.util.MultiException;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.net.BindException;

import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.util.logging.Logger;

/**
 * The configuration handler for the main Pulse web application.
 */
public class PulseWebappConfigurationHandler implements ServerConfigurationHandler
{
    private static final Logger LOG = Logger.getLogger(PulseWebappConfigurationHandler.class);

    private static final String[] LOGGING_IGNORE_PATHS = new String[]{"/images/*.*", "*.css", "*.js", "*.ico", "*.gif"};

    private ConfigurationManager configurationManager;

    /**
     * The temporary directory available to the web applications.
     */
    private File tmpDir;

    /**
     * The directory into which log files are to be written.
     */
    private File logDir;

    public void configure(Server server) throws IOException
    {
        SystemConfiguration config = configurationManager.getSystemConfig();

        HttpListener listener;

        if (config.isSslEnabled())
        {
            SslListener sslListener = new SslListener();
            sslListener.setPassword(config.getSslPassword());
            sslListener.setKeyPassword(config.getSslKeyPassword());
            if (config.getSslKeystore() != null)
            {
                sslListener.setKeystore(config.getSslKeystore());
            }

            listener = sslListener;
        }
        else
        {
            listener = new SocketListener();
        }

        listener.setHost(config.getBindAddress());
        listener.setPort(config.getServerPort());

        server.addListener(listener);

        try
        {
            WebApplicationContext appContext = server.addWebApplication(
                    config.getContextPath(), configurationManager.getSystemPaths().getContentRoot().getAbsolutePath());
            appContext.setDefaultsDescriptor(null);
            appContext.setAttribute("javax.servlet.context.tempdir", tmpDir);

            if (isRequestLoggingEnabled())
            {
                NCSARequestLog requestLog = new NCSARequestLog();
                requestLog.setAppend(false);
                requestLog.setExtended(isRequestLoggingExtended());
                requestLog.setIgnorePaths(getRequestLoggingIgnorePaths());
                requestLog.setRetainDays(getDaysLogsRetained());
                requestLog.setFilename(new File(logDir, "yyyy_mm_dd.request.log").getAbsolutePath());
                appContext.setRequestLog(requestLog);
            }

            server.start();
        }
        catch(MultiException e)
        {
            for(Exception nested: (List<Exception>)e.getExceptions())
            {
                if (nested instanceof BindException)
                {
                    handleBindException(config);
                }
                else
                {
                    LOG.severe("Unable to start server: " + nested.getMessage(), nested);
                }
            }

            // This is fatal.
            System.exit(1);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to start server: " + e.getMessage(), e);

            // This is fatal.
            System.exit(1);
        }
    }

    private String[] getRequestLoggingIgnorePaths()
    {
        return LOGGING_IGNORE_PATHS;
    }

    private Integer getDaysLogsRetained()
    {
        return Integer.getInteger("pulse.request.logging.retain.days", 30);
    }

    private boolean isRequestLoggingExtended()
    {
        return Boolean.getBoolean("pulse.extended.request.logging");
    }

    private boolean isRequestLoggingEnabled()
    {
        return Boolean.getBoolean("pulse.enable.request.logging");
    }

    private void handleBindException(SystemConfiguration config)
    {
        LOG.severe(String.format("Unable to start on port %s because it " +
                "is being used by another process.  Please select a different port and restart pulse.", config.getServerPort()));
    }


    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setLogDir(File logDir)
    {
        this.logDir = logDir;
    }

    public void setTmpDir(File tmpDir)
    {
        this.tmpDir = tmpDir;
    }
}
