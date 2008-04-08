package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PostProcessor;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.plugins.AbstractExtensionManager;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing post-processors (e.g. JUnit report
 * processing).
 */
public class PostProcessorExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(PostProcessorExtensionManager.class);

    private PulseFileLoaderFactory fileLoaderFactory;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.postprocessors";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String cls = config.getAttribute("class");

        Class clazz = loadClass(extension, cls);
        if(clazz == null)
        {
            LOG.severe(String.format("Ignoring post-processor '%s': class '%s' does not exist", name, cls));
            return;
        }

        if(!PostProcessor.class.isAssignableFrom(clazz))
        {
            LOG.severe(String.format("Ignoring post-processor '%s': class '%s' does not implement PostProcessor", name, cls));
            return;
        }

        System.out.printf("Adding Post-Processor: %s -> %s\n", name, cls);
        fileLoaderFactory.register(name, clazz);
        tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
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
}
