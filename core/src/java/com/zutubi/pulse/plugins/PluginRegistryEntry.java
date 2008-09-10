package com.zutubi.pulse.plugins;

import com.zutubi.util.TextUtils;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 *
 */
public class PluginRegistryEntry
{
    public static final String PLUGIN_STATE_KEY = "plugin.state";
    public static final String PLUGIN_TYPE_KEY = "plugin.type";
    public static final String PLUGIN_VERSION_KEY = "plugin.version";
    public static final String PLUGIN_SOURCE_KEY = "plugin.uri";
    public static final String PLUGIN_PENDING_KEY = "plugin.pending.action";
    public static final String UPGRADE_SOURCE_KEY = "plugin.upgrade.source";

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
        return get(UPGRADE_SOURCE_KEY);
    }

    public String getPendingAction()
    {
        return get(PLUGIN_PENDING_KEY);
    }

    public String getSource()
    {
        return get(PLUGIN_SOURCE_KEY);
    }

    public PluginManager.State getState()
    {
        String stateStr = get(PLUGIN_STATE_KEY);
        if (TextUtils.stringSet(stateStr))
        {
            return PluginManager.State.valueOf(stateStr);
        }
        return null;
    }

    public void setState(PluginManager.State state)
    {
        put(PLUGIN_STATE_KEY, state.toString());
    }

    public Plugin.Type getType()
    {
        String typeStr = get(PLUGIN_TYPE_KEY);
        if (TextUtils.stringSet(typeStr))
        {
            return Plugin.Type.valueOf(typeStr);
        }
        return null;
    }

    public void setType(Plugin.Type type)
    {
        put(PLUGIN_TYPE_KEY, type.toString());
    }

    public PluginVersion getVersion()
    {
        String versionString = get(PLUGIN_VERSION_KEY);
        if (!TextUtils.stringSet(versionString))
        {
            return null;
        }
        return new PluginVersion(versionString);
    }

    public void setVersion(PluginVersion version)
    {
        put(PLUGIN_VERSION_KEY, version.toString());
    }

    public Collection<String> keySet()
    {
        return this.entry.keySet();
    }

    public boolean containsKey(String key)
    {
        return this.entry.containsKey(key);
    }

    public void remove(String key)
    {
        this.entry.remove(key);
    }

}
