package com.zutubi.pulse.plugins;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 */
public interface PluginManager
{
    IExtensionRegistry getExtenstionRegistry();
    IExtensionTracker getExtenstionTracker();
}
