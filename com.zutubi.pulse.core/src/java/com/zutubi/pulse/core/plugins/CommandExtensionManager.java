package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeException;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing build commands (e.g. the Ant command).
 */
public class CommandExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(CommandExtensionManager.class);

    private PulseFileLoaderFactory fileLoaderFactory;
    private TypeRegistry typeRegistry;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.commands";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String cls = config.getAttribute("class");

        Class clazz = loadClass(extension, cls);
        if(clazz != null)
        {
            if (!Configuration.class.isAssignableFrom(clazz))
            {
                LOG.severe(String.format("Ignoring command '%s': class '%s' does not implement Configuration", name, cls));
                return;
            }

            // FIXME loader better error reporting
            try
            {
                typeRegistry.register(clazz);
            }
            catch (TypeException e)
            {
                LOG.severe(e);
                return;
            }

            if (PluginManager.VERBOSE_EXTENSIONS)
            {
                System.out.println(String.format("Adding Command: %s -> %s", name, cls));
            }
            fileLoaderFactory.register(name, clazz);
            tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for (Object o : objects)
        {
            fileLoaderFactory.unregister((String) o);
        }
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
