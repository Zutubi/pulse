package com.zutubi.pulse.servercore;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.RecipeUtils;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.util.Constants;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 */
public class SystemInfo
{
    private Properties systemProperties;
    private Map<String, String> coreProperties;
    private Version version;
    private Map<String, String> environment;
    private long totalMemory;
    private long usedMemory;
    private long freeMemory;

    public static SystemInfo getSystemInfo(ConfigurationManager configurationManager, StartupManager startupManager)
    {
        DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss z");

        SystemInfo info = new SystemInfo();
        info.coreProperties = configurationManager.getCoreProperties();

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
        
        info.environment = new HashMap<String, String>(System.getenv());
        for (String suppressed: RecipeUtils.getSuppressedEnvironment())
        {
            if (info.environment.containsKey(suppressed))
            {
                info.environment.put(suppressed, RecipeUtils.SUPPRESSED_VALUE);
            }
        }

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

    public Map<String, String> getCoreProperties()
    {
        return coreProperties;
    }

    public Version getVersion()
    {
        return version;
    }

    public Map<String, String> getEnvironment()
    {
        return environment;
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
