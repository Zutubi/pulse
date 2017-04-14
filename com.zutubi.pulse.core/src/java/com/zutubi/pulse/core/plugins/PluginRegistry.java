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

import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The plugin registry is used to store persistent information / settings for plugins.
 */
public class PluginRegistry
{
    private static final Logger LOG = Logger.getLogger(PluginRegistry.class);

    private static final String ELEMENT_REGISTRY = "registry";
    private static final String ELEMENT_PLUGINS = "plugins";
    private static final String ELEMENT_PLUGIN = "plugin";
    private static final String ATTRIBUTE_ID = "id";

    /**
     * The name of the registry file.
     */
    static final String REGISTRY_FILE_NAME = "plugin-registry.xml";

    /**
     * The file in which all of the registry details are persisted.
     */
    private File registry;

    private Map<String, PluginRegistryEntry> entries = new HashMap<String, PluginRegistryEntry>();

    /**
     * @param dir the dir containing the persistent registry information.
     * @throws java.io.IOException if the specified registry file is invalid, ie: empty, poorly formed..
     */
    public PluginRegistry(File dir) throws IOException
    {
        this.registry = new File(dir, REGISTRY_FILE_NAME);

        load();
    }

    /**
     * Register a new plugin
     *
     * @param id uniquely identifying the plugin
     * @return an entry for the new plugin
     */
    public PluginRegistryEntry register(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Plugin ID can not be null.");
        }
        
        if (!isRegistered(id))
        {
            entries.put(id, new PluginRegistryEntry());
        }
        return entries.get(id);
    }

    public PluginRegistryEntry register(Plugin plugin)
    {
        return register(plugin.getId());
    }

    /**
     * Check if a registry entry for the specified id exists
     *
     * @param id uniquely identifying the plugin
     * @return true if the plugin has a registry entry, false otherwise.
     */
    public boolean isRegistered(String id)
    {
        return entries.containsKey(id);
    }

    public boolean isRegistered(Plugin plugin)
    {
        return isRegistered(plugin.getId());
    }

    public List<String> getRegistrations()
    {
        return new LinkedList<String>(entries.keySet());
    }

    public PluginRegistryEntry getEntry(String id)
    {
        return entries.get(id);
    }

    /**
     * Flush any changes to the registry to the persistent storage.
     *
     * @throws IOException if there is a problem persisting the registry.
     */
    public void flush() throws IOException
    {
        Element root = new Element(ELEMENT_REGISTRY);
        Document doc = new Document(root);

        // write plugins.
        Element plugins = new Element(ELEMENT_PLUGINS);
        root.appendChild(plugins);

        for (String id : entries.keySet())
        {
            Element element = new Element(ELEMENT_PLUGIN);
            element.addAttribute(new Attribute(ATTRIBUTE_ID, id));

            PluginRegistryEntry entry = entries.get(id);
            for (String key : entry.keySet())
            {
                Element e = new Element(key);
                e.appendChild(entry.get(key));
                element.appendChild(e);
            }
            plugins.appendChild(element);
        }

        BufferedOutputStream bos = null;
        try
        {
            // ensure that we can write to the registry.
            File registryDir = registry.getParentFile();
            if (!registryDir.exists() && !registryDir.mkdirs())
            {
                throw new IOException("Failed to create " + registryDir.getAbsolutePath());
            }
            if (!registryDir.isDirectory())
            {
                throw new IOException("Failed to create plugin registry file. Cause: " + registryDir.getAbsolutePath() + " is not a directory.");
            }

            FileOutputStream fos = new FileOutputStream(registry);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            serializer.write(doc);
            serializer.flush();
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    protected void load() throws IOException
    {
        entries.clear();

        if (!registry.exists())
        {
            // no loading required... or throw new IOException ?
            return;
        }

        Document doc;
        FileInputStream input = null;
        try
        {
            input = new FileInputStream(registry);
            Builder builder = new Builder();
            doc = builder.build(input);
        }
        catch (ParsingException pex)
        {
            throw new IOException("Unable to parse plugins file '" + registry.getAbsolutePath() + "', Cause:" + pex.getMessage());
        }
        finally
        {
            IOUtils.close(input);
        }

        if (doc.getRootElement() == null)
        {
            // under what conditions would this happen? ... do we need this check?
            return;
        }

        // read the plugins details.
        Elements pluginsElements = doc.getRootElement().getChildElements(ELEMENT_PLUGINS);
        if (pluginsElements.size() > 0)
        {
            Element pluginsElement = pluginsElements.get(0);
            Elements pluginEntries = pluginsElement.getChildElements(ELEMENT_PLUGIN);
            for (int i = 0; i < pluginEntries.size(); i++)
            {
                Element pluginEntry = pluginEntries.get(i);
                String id = pluginEntry.getAttributeValue(ATTRIBUTE_ID);

                // a missing id means an invalid entry. log and drop.
                if (id == null || id.equals(""))
                {
                    LOG.warning("registry entry is missing required 'id' field. Ignorning.");
                    continue;
                }

                PluginRegistryEntry entry = new PluginRegistryEntry();

                Elements children = pluginEntry.getChildElements();
                for (int j = 0; j < children.size(); j++)
                {
                    Element child = children.get(j);
                    entry.put(child.getLocalName(), child.getValue());
                }
                entries.put(id, entry);
            }
        }
    }

    public boolean unregister(String id)
    {
        return entries.remove(id) != null;
    }
}
