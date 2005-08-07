package com.cinnamonbob.web.console;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.bootstrap.StartupManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 */
public class SystemInfoAction extends ActionSupport
{
    private Runtime runtime = Runtime.getRuntime();

    private Properties props;

    private StartupManager startupManager;

    private DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    private static final long MEGABYTE = 1048576;

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    /**
     *
     * @return
     */
    public String execute()
    {
        // initialise the props object.
        Properties systemProperties = System.getProperties();
        props = new Properties();
        props.putAll(systemProperties);
        props.put("system.date", dateFormatter.format(new Date()));
        props.put("system.time", timeFormatter.format(new Date()));
        // record the time when the system startup
        props.put("system.uptime", formatUptime(startupManager.getUptime()));

        props.put("os.name", systemProperties.getProperty("os.name") + " " + systemProperties.getProperty("os.version"));

        return SUCCESS;
    }

    /**
     *
     * @return
     */
    public Map getProperties()
    {
        return props;
    }

    /**
     * Get the total memory available to the runtime in MBs.
     * @return
     *
     * @see Runtime#totalMemory()
     */
    public long getTotalMemory()
    {
        return runtime.totalMemory() / MEGABYTE;
    }

    /**
     * Get the total free memory available to the runtime in MBs.
     * @return
     * @see Runtime#freeMemory()
     */
    public long getFreeMemory()
    {
        return runtime.freeMemory() / MEGABYTE;
    }

    /**
     * Get the memory used by the runtime in MBs. This is the difference between tht total memory available
     * to the runtime and the current free memory amount. 
     * @return
     */
    public long getUsedMemory()
    {
        return getTotalMemory() - getFreeMemory();
    }

    // extract this into a formatter that can be used by the UI to define the format of the information.
    public long getPercentage(long a, long b)
    {
        return (long) ((((float)a)/((float)b)) * 100);
    }

    // extract this into a formatter that can be used by the UI to define the format of the information.
    private String formatUptime(long uptime)
    {
        StringBuffer buffer = new StringBuffer();
        String sep = "";
        long days = uptime / DAY;
        if (days > 0)
        {
            buffer.append(sep + days + " day" + ((days != 1) ? "s" : ""));
            sep = " ";
        }
        long hours = uptime % DAY / HOUR;
        long minutes = uptime % DAY % HOUR / MINUTE;
        long seconds = uptime % DAY % HOUR % MINUTE / SECOND;
        buffer.append(sep + MessageFormat.format("{0,number,00}:{1,number,00}:{2,number,00}", hours, minutes, seconds));
        return buffer.toString();
    }
}
