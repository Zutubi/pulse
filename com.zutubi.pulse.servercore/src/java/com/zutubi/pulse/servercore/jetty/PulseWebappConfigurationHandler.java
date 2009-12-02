package com.zutubi.pulse.servercore.jetty;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.util.Constants;
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
    private static final String PROPERTY_IDLE_TIMEOUT = "pulse.jetty.idle.timeout";
    private static final int DEFAULT_IDLE_TIMEOUT = (int) (60 * Constants.SECOND);
    private static final String PROPERTY_MIN_THREADS = "pulse.jetty.min.threads";
    private static final String PROPERTY_MAX_THREADS = "pulse.jetty.max.threads";

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

        SocketListener listener;

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
        listener.setMaxIdleTimeMs(getIdleTimeout());
        listener.setMinThreads(Integer.getInteger(PROPERTY_MIN_THREADS, listener.getMinThreads()));
        listener.setMaxThreads(Integer.getInteger(PROPERTY_MAX_THREADS, listener.getMaxThreads()));
        server.addListener(listener);

    }

    private int getIdleTimeout()
    {
        String timeoutValue = System.getProperty(PROPERTY_IDLE_TIMEOUT);
        if (TextUtils.stringSet(timeoutValue))
        {
            try
            {
                return (int) (Integer.parseInt(timeoutValue) * Constants.SECOND);
            }
            catch (NumberFormatException e)
            {
                return DEFAULT_IDLE_TIMEOUT;
            }
        }
        else
        {
            return DEFAULT_IDLE_TIMEOUT;
        }
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
