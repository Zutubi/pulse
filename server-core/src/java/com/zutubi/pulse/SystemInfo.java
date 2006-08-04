package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Startup;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.util.Constants;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 */
public class SystemInfo
{
    private Properties systemProperties;
    private Properties paths;
    private Version version;
    private long totalMemory;
    private long usedMemory;
    private long freeMemory;

    public static SystemInfo getSystemInfo(ConfigurationManager configurationManager, Startup startupManager)
    {
        DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss z");

        SystemInfo info = new SystemInfo();
        info.paths = new Properties();

        EnvConfig envConfig = configurationManager.getEnvConfig();
        info.paths.put("pulse.homeDir.field", envConfig.getPulseHome());

        // TODO: show the pulse config path details... and while we are at it, show the system config details as well.
        //info.paths.put("pulse.config.field", envConfig.getPulseConfig());

        info.paths.put("pulse.dataDir.field", configurationManager.getUserPaths().getData());

        Properties properties = System.getProperties();
        info.systemProperties = new Properties();
        info.systemProperties.putAll(properties);
        info.systemProperties.put("system.date", dateFormatter.format(new Date()));
        info.systemProperties.put("system.time", timeFormatter.format(new Date()));

        // record the time when the system startup
        info.systemProperties.put("system.uptime", formatUptime(startupManager.getUptime()));

        info.systemProperties.put("os.name", properties.getProperty("os.name") + " " + properties.getProperty("os.version"));

        info.totalMemory = Runtime.getRuntime().totalMemory() / Constants.MEGABYTE;
        info.freeMemory = Runtime.getRuntime().freeMemory() / Constants.MEGABYTE;
        info.usedMemory = info.totalMemory - info.freeMemory;

        info.systemProperties.put("memory.used", info.usedMemory);
        info.systemProperties.put("memory.free", info.freeMemory);
        info.systemProperties.put("memory.total", info.totalMemory);

        info.version = Version.getVersion();
        return info;
    }

    private static String formatUptime(long uptime)
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

    public Properties getSystemProperties()
    {
        return systemProperties;
    }

    public Properties getPaths()
    {
        return paths;
    }

    public Version getVersion()
    {
        return version;
    }

    public long getTotalMemory()
    {
        return totalMemory;
    }

    public long getUsedMemory()
    {
        return usedMemory;
    }

    public long getFreeMemory()
    {
        return freeMemory;
    }
}
