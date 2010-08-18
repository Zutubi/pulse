package com.zutubi.pulse.core.plugins;

import com.zutubi.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PluginRegistryEntry
{
    // The mode key is named "state" for historical reasons.
    private static final String KEY_PLUGIN_MODE = "plugin.state";
    private static final String KEY_PLUGIN_SOURCE = "plugin.uri";
    private static final String KEY_PLUGIN_PENDING = "plugin.pending.action";
    private static final String KEY_UPGRADE_SOURCE = "plugin.upgrade.source";

    /**
     * Persistent plugin modes.  These are processed on startup to decide which
     * state the plugin should be moved to.
     */
    public enum Mode
    {
        /**
         * The normal situation: the plugin should be enabled on startup.
         */
        ENABLE,
        /**
         * The plugin exists but should not be deployed on startup.
         */
        DISABLE,
        /**
         * The plugin does not exist.  This mode is used to keep prepackaged
         * plugins uninstalled (even across Pulse upgrades).
         */
        UNINSTALLED
    }
    
    private Map<String, String> entry;

    public PluginRegistryEntry()
    {
        this(new HashMap<String, String>());
    }

    public PluginRegistryEntry(Map<String, String> entry)
    {
        this.entry = entry;
    }

    public void put(String key, String value)
    {
        this.entry.put(key, value);
    }

    public String get(String key)
    {
        return this.entry.get(key);
    }

    public String getUpgradeSource()
    {
        return get(KEY_UPGRADE_SOURCE);
    }

    public void setUpgradeSource(String source)
    {
        put(KEY_UPGRADE_SOURCE, source);
    }
    
    public void removeUpgradeSource()
    {
        remove(KEY_UPGRADE_SOURCE);
    }

    public boolean hasPendingAction()
    {
        return StringUtils.stringSet(getPendingAction());
    }
    
    public String getPendingAction()
    {
        return get(KEY_PLUGIN_PENDING);
    }

    public void setPendingAction(String action)
    {
        put(KEY_PLUGIN_PENDING, action);
    }
    
    public void removePendingAction()
    {
        remove(KEY_PLUGIN_PENDING);
    }

    public boolean hasSource()
    {
        return StringUtils.stringSet(getSource());
    }

    public String getSource()
    {
        return get(KEY_PLUGIN_SOURCE);
    }

    public void setSource(String str)
    {
        put(KEY_PLUGIN_SOURCE, str);
    }

    public void removeSource()
    {
        remove(KEY_PLUGIN_SOURCE);
    }
    
    public Mode getMode()
    {
        String stateStr = get(KEY_PLUGIN_MODE);
        if (StringUtils.stringSet(stateStr))
        {
            try
            {
                return Mode.valueOf(stateStr);
            }
            catch (IllegalArgumentException e)
            {
                // Fall through.
            }
        }
        
        return Mode.ENABLE;
    }

    public void setMode(Mode mode)
    {
        put(KEY_PLUGIN_MODE, mode.toString());
    }

    public Collection<String> keySet()
    {
        return entry.keySet();
    }

    public boolean containsKey(String key)
    {
        return entry.containsKey(key);
    }

    public void remove(String key)
    {
        entry.remove(key);
    }
}
