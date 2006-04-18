/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.console;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Home;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.web.ActionSupport;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 */
public class SystemInfoAction extends ActionSupport
{
    private Properties props;
    private License license;
    private Version version;

    private StartupManager startupManager;
    private ConfigurationManager configurationManager;

    private DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss z");

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     *
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

        Home home = configurationManager.getHome();
        license = home.getLicense();
        version = home.getVersion();

        return SUCCESS;
    }

    /**
     * Retrieve a set of runtime properties.
     *
     */
    public Map getProperties()
    {
        return props;
    }

    /**
     * Retrieve the currently installed license.
     *
     * @return license
     */
    public License getLicense()
    {
        return license;
    }

    public String getExpiryDate()
    {
        if (license.expires())
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(license.getExpiryDate());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return dateFormatter.format(cal.getTime());
        }
        return "Never";
    }

    /**
     * Retrieve the current home version.
     *
     * @return version
     */
    public Version getVersion()
    {
        return version;
    }

    /**
     * Get the total memory available to the runtime in MBs.
     * @return
     *
     * @see Runtime#totalMemory()
     */
    public long getTotalMemory()
    {
        return Runtime.getRuntime().totalMemory() / Constants.MEGABYTE;
    }

    /**
     * Get the total free memory available to the runtime in MBs.
     * @return
     * @see Runtime#freeMemory()
     */
    public long getFreeMemory()
    {
        return Runtime.getRuntime().freeMemory() / Constants.MEGABYTE;
    }

    /**
     * Get the memory used by the runtime in MBs. This is the difference between tht total memory available
     * to the runtime and the current free memory amount. 
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
        long days = uptime / Constants.DAY;
        if (days > 0)
        {
            buffer.append(sep).append(days).append(" day").append(((days != 1) ? "s" : ""));
            sep = " ";
        }
        long hours = uptime % Constants.DAY / Constants.HOUR;
        long minutes = uptime % Constants.DAY % Constants.HOUR / Constants.MINUTE;
        long seconds = uptime % Constants.DAY % Constants.HOUR % Constants.MINUTE / Constants.SECOND;
        buffer.append(sep).append(MessageFormat.format("{0,number,00}:{1,number,00}:{2,number,00}", hours, minutes, seconds));
        return buffer.toString();
    }
}
