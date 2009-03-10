package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import org.mortbay.http.HttpListener;
import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * The configuration handler for the main Pulse web application.
 */
public class PulseWebappConfigurationHandler implements ServerConfigurationHandler, WebappConfigurationHandler
{
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

    }

    public void configure(WebApplicationContext context) throws IOException
    {
        SystemConfiguration config = configurationManager.getSystemConfig();

        context.setWAR(configurationManager.getSystemPaths().getContentRoot().getAbsolutePath());
        context.setContextPath(config.getContextPath());
        context.setDefaultsDescriptor(null);

        if(!tmpDir.exists() && !tmpDir.mkdirs())
        {
            throw new IOException("Failed to create " + tmpDir.getCanonicalPath());
        }

        context.setAttribute("javax.servlet.context.tempdir", tmpDir);

        if (isRequestLoggingEnabled())
        {
            NCSARequestLog requestLog = new NCSARequestLog();
            requestLog.setAppend(false);
            requestLog.setExtended(isRequestLoggingExtended());
            requestLog.setIgnorePaths(getRequestLoggingIgnorePaths());
            requestLog.setRetainDays(getDaysLogsRetained());
            requestLog.setFilename(new File(logDir, "yyyy_mm_dd.request.log").getAbsolutePath());
            context.setRequestLog(requestLog);
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
