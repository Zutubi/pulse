/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.resources.api.CompositeResourceLocator;
import com.zutubi.pulse.core.resources.api.ResourceLocator;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extension manager for managing resource locators.
 */
public class ResourceLocatorExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(ResourceLocatorExtensionManager.class);

    /**
     * Lock to control multiple thread access to the info map.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<String, ResourceLocatorDescriptor> descriptors = new HashMap<String, ResourceLocatorDescriptor>();

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.resourcelocators";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String cls = config.getAttribute("class");

        Class clazz = loadClass(extension, cls);
        if(clazz == null)
        {
            LOG.severe(String.format("Ignoring resource locator '%s': class '%s' does not exist", name, cls));
            return;
        }

        if (!ResourceLocator.class.isAssignableFrom(clazz))
        {
            LOG.severe(String.format("Ignoring resource locator '%s': class '%s' does not implement ResourceLocator", name, cls));
            return;
        }

        @SuppressWarnings({"unchecked"})
        ResourceLocatorDescriptor descriptor = new ResourceLocatorDescriptor(name, clazz);

        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.printf("Adding Resource Locator: %s -> %s\n", name, cls);
        }

        lock.writeLock().lock();
        try
        {
            descriptors.put(name, descriptor);
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
                descriptors.remove(name);
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieve a copy of the currently registered resource locators.  
     *
     * @return a copy of the registered list post processors.
     */
    public Collection<ResourceLocatorDescriptor> getResourceLocators()
    {
        LinkedList<ResourceLocatorDescriptor> copy;
        lock.readLock().lock();
        try
        {
            copy = new LinkedList<ResourceLocatorDescriptor>(descriptors.values());
        }
        finally
        {
            lock.readLock().unlock();
        }
        return copy;
    }

    /**
     * Creates and returns a discoverer that will use all registered locators.
     * 
     * @return a new discoverer based on plugged-in locators
     */
    public ResourceDiscoverer createResourceDiscoverer()
    {
        Collection<ResourceLocatorDescriptor> descriptors = getResourceLocators();
        List<ResourceLocator> locators = new LinkedList<ResourceLocator>();
        for (ResourceLocatorDescriptor descriptor: descriptors)
        {
            try
            {
                ResourceLocator locator = descriptor.getClazz().newInstance();
                locators.add(locator);
            }
            catch (Exception e)
            {
                LOG.severe("Unable to instantiate locator '" + descriptor.getName() + "':" + e.getMessage(), e);
            }
        }
        
        return new ResourceDiscoverer(new CompositeResourceLocator(locators.toArray(new ResourceLocator[locators.size()])));
    }
    
}
