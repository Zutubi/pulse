package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extension manager for managing post-processors (e.g. JUnit report
 * processing).
 */
public class PostProcessorExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(PostProcessorExtensionManager.class);

    /**
     * Lock to control multiple thread access to the info map.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<String, PostProcessorDescriptor> descriptors = new HashMap<String, PostProcessorDescriptor>();
    private PulseFileLoaderFactory fileLoaderFactory;
    private ConfigurationRegistry configurationRegistry;

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

        if (!PostProcessorConfiguration.class.isAssignableFrom(clazz))
        {
            LOG.severe(String.format("Ignoring post-processor '%s': class '%s' does not implement PostProcessorConfiguration", name, cls));
            return;
        }

        try
        {
            configurationRegistry.registerConfigurationType(clazz);
        }
        catch (TypeException e)
        {
            LOG.severe("Registering post-processor '" + name + "': " + e.getMessage(), e);
            return;
        }
        
        String displayName = ConfigUtils.getString(config, "display-name", name);
        boolean contributeDefault = ConfigUtils.getBoolean(config, "default-processor", false);
        PostProcessorDescriptor descriptor = new PostProcessorDescriptor(name, displayName, contributeDefault, clazz);

        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.printf("Adding Post-Processor: %s -> %s\n", name, cls);
        }

        lock.writeLock().lock();
        try
        {
            descriptors.put(name, descriptor);
            fileLoaderFactory.register(name, clazz);
        }
        finally
        {
            lock.writeLock().unlock();
        }

        tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        lock.writeLock().lock();
        try
        {
            for (Object o : objects)
            {
                String name = (String) o;
                fileLoaderFactory.unregister(name);
                descriptors.remove(name);
                // ... Support unregistration in the configuration registry
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public String getDefaultProcessorName(final Class<? extends PostProcessorConfiguration> type)
    {
        PostProcessorDescriptor descriptor;
        lock.readLock().lock();
        try
        {
            descriptor = CollectionUtils.find(descriptors.values(), new Predicate<PostProcessorDescriptor>()
            {
                public boolean satisfied(PostProcessorDescriptor postProcessorDescriptor)
                {
                    return postProcessorDescriptor.getClazz() == type;
                }
            });
        }
        finally
        {
            lock.readLock().unlock();
        }

        if (descriptor == null)
        {
            return null;
        }

        return descriptor.getDisplayName();
    }

    public PostProcessorDescriptor getPostProcessor(String name)
    {
        lock.readLock().lock();
        try
        {
            return descriptors.get(name);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieve a copy of the currently registered post processors.  
     *
     * @return a copy of the registered list post processors.
     */
    public Collection<PostProcessorDescriptor> getPostProcessors()
    {
        LinkedList<PostProcessorDescriptor> copy;
        lock.readLock().lock();
        try
        {
            copy = new LinkedList<PostProcessorDescriptor>(descriptors.values());
        }
        finally
        {
            lock.readLock().unlock();
        }
        return copy;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
