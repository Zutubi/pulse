package com.zutubi.pulse.hessian;

import com.zutubi.pulse.plugins.AbstractExtensionManager;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.Map;
import java.util.HashMap;

/**
 * TODO: This may be used by hessian, but the functionality is something that can easily be supported by the
 * TODO: configuration extension manager.
 *
 */
public class HessianConfigurationExtensionManager extends AbstractExtensionManager
{
    private final Map<String, String> contributorMapping = new HashMap<String, String>();

    public void init()
    {
        super.init();
    }

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.config";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String className = config.getAttribute("class");
        String contributor = extension.getContributor().getName();

        contributorMapping.put(className, contributor);
    }

    public void removeExtension(IExtension iExtension, Object[] objects)
    {

    }

    public String getContributor(String className)
    {
        return contributorMapping.get(className);
    }
}
