package com.zutubi.pulse.plugins;

import com.zutubi.pulse.api.PulseFileElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.internal.registry.ExtensionRegistry;

/**
 */
public interface PluginManager
{
    ExtensionRegistry getExtenstionRegistry();
    IExtensionTracker getExtenstionTracker();
}
