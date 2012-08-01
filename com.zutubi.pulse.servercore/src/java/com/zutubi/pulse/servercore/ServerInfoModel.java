package com.zutubi.pulse.servercore;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.RecipeUtils;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.util.Constants;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Holds information about a server running Pulse (master or agent).
 */
public class ServerInfoModel
{
    private static final Messages I18N = Messages.getInstance(ServerInfoModel.class);
    
    private Map<String, String> serverProperties;
    private Map<String, String> pulseProperties;
    private Map<String, String> jvmProperties;
    private Map<String, String> environment;

    public static ServerInfoModel getServerInfo(ConfigurationManager configurationManager, StartupManager startupManager, boolean includeDetailed)
    {
        DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss z");
        Properties properties = System.getProperties();

        ServerInfoModel info = new ServerInfoModel();

        info.serverProperties = new HashMap<String, String>();
        info.serverProperties.put(I18N.format("system.date"), dateFormatter.format(new Date()));
        info.serverProperties.put(I18N.format("system.time"), timeFormatter.format(new Date()));
        info.serverProperties.put(I18N.format("system.uptime"), formatUptime(startupManager.getUptime()));
        info.serverProperties.put(I18N.format("os.name"), properties.getProperty("os.name") + " " + properties.getProperty("os.version"));

        long totalMemory = Runtime.getRuntime().totalMemory() / Constants.MEGABYTE;
        long freeMemory = Runtime.getRuntime().freeMemory() / Constants.MEGABYTE;
        long usedMemory = totalMemory - freeMemory;

        info.serverProperties.put(I18N.format("memory.used"), Long.toString(usedMemory));
        info.serverProperties.put(I18N.format("memory.free"), Long.toString(freeMemory));
        info.serverProperties.put(I18N.format("memory.total"), Long.toString(totalMemory));
        info.serverProperties.put(I18N.format("default.locale"), Locale.getDefault().toString());
        info.serverProperties.put(I18N.format("java.name"), properties.getProperty("java.vm.name"));
        info.serverProperties.put(I18N.format("java.vendor"), properties.getProperty("java.vm.vendor"));
        info.serverProperties.put(I18N.format("java.version"), properties.getProperty("java.version"));

        Map<String, String> coreProperties = configurationManager.getCoreProperties();
        Version version = Version.getVersion();
        info.pulseProperties = new HashMap<String, String>();
        info.pulseProperties.put(I18N.format("version.number"), version.getVersionNumber());
        info.pulseProperties.put(I18N.format("build.number"), version.getBuildNumber());
        for (Map.Entry<String, String> coreEntry: coreProperties.entrySet())
        {
            info.pulseProperties.put(I18N.format(coreEntry.getKey()), coreEntry.getValue());
        }

        if (includeDetailed)
        {
            info.jvmProperties = new HashMap<String, String>();
            for (Map.Entry entry: properties.entrySet())
            {
                info.jvmProperties.put((String) entry.getKey(), (String) entry.getValue());
            }

            info.environment = new HashMap<String, String>(System.getenv());
            for (String suppressed: RecipeUtils.getSuppressedEnvironment())
            {
                if (info.environment.containsKey(suppressed))
                {
                    info.environment.put(suppressed, RecipeUtils.SUPPRESSED_VALUE);
                }
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

    public Map<String, String> getServerProperties()
    {
        return serverProperties;
    }

    public Map<String, String> getPulseProperties()
    {
        return pulseProperties;
    }

    public Map<String, String> getJvmProperties()
    {
        return jvmProperties;
    }

    public Map<String, String> getEnvironment()
    {
        return environment;
    }
}
