package com.zutubi.pulse.core.plugins.repository;

import com.google.common.base.Function;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A utility class for working with lists of plugins and plugin infos.
 */
public class PluginList
{
    private static final Logger LOG = Logger.getLogger(PluginList.class);

    public static final String ELEMENT_PLUGINS   = "plugins";
    public static final String ELEMENT_PLUGIN    = "plugin";
    public static final String ATTRIBUTE_ID      = "id";
    public static final String ATTRIBUTE_VERSION = "version";
    public static final String ATTRIBUTE_SCOPE   = "scope";

    private static final String KEY_ID      = "id";
    private static final String KEY_VERSION = "version";
    private static final String KEY_SCOPE   = "scope";
    
    /**
     * Converts a list of plugins to corresponding plugins infos.
     * 
     * @param plugins the plugins to convert
     * @return a list of infos corresponding to the input
     */
    public static List<PluginInfo> toInfos(Iterable<Plugin> plugins)
    {
        return newArrayList(transform(plugins, new Function<Plugin, PluginInfo>()
        {
            public PluginInfo apply(Plugin plugin)
            {
                return new PluginInfo(plugin.getId(), plugin.getVersion().toString(), getScope(plugin));
            }
        }));
    }

    /**
     * Converts a list of plugins to corresponding hashtables of plugin info.
     * 
     * @param plugins the plugins to convert
     * @return a list of hashstables corresponding to the input
     * 
     * @see #infosToHashes(Iterable) 
     * @see #infosFromHashes(Iterable)
     */
    public static List<Hashtable<String, Object>> pluginsToHashes(Iterable<Plugin> plugins)
    {
        return newArrayList(transform(plugins, new Function<Plugin, Hashtable<String, Object>>()
        {
            public Hashtable<String, Object> apply(Plugin plugin)
            {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                hash.put(KEY_ID, plugin.getId());
                hash.put(KEY_VERSION, plugin.getVersion().toString());
                hash.put(KEY_SCOPE, getScope(plugin).toString());
                return hash;
            }
        }));
    }

    /**
     * Converts a list of plugin infos to corresponding hashtables.
     * 
     * @param pluginInfos the infos to convert
     * @return a list of hashstables corresponding to the input
     * 
     * @see #pluginsToHashes(Iterable) 
     * @see #infosFromHashes(Iterable)
     */
    public static List<Hashtable<String, Object>> infosToHashes(Iterable<PluginInfo> pluginInfos)
    {
        return newArrayList(transform(pluginInfos, new Function<PluginInfo, Hashtable<String, Object>>()
        {
            public Hashtable<String, Object> apply(PluginInfo info)
            {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                hash.put(KEY_ID, info.getId());
                hash.put(KEY_VERSION, info.getVersion());
                hash.put(KEY_SCOPE, info.getScope().name());
                return hash;
            }
        }));
    }
    
    /**
     * Converts a list hashtables to corresponding plugin infos.
     * 
     * @param hashes the hashes to convert
     * @return a list of plugin infors corresponding to the input
     * 
     * @see #infosToHashes(Iterable) 
     * @see #pluginsToHashes(Iterable) 
     */
    public static List<PluginInfo> infosFromHashes(Iterable<Hashtable<String, Object>> hashes)
    {
        List<PluginInfo> result = new LinkedList<PluginInfo>();
        for (Hashtable<String, Object> hash: hashes)
        {
            if (keysValid(hash))
            {
                String id = (String) hash.get(KEY_ID);
                String version = (String) hash.get(KEY_VERSION);
                String scope = (String) hash.get(KEY_SCOPE);

                try
                {
                    result.add(new PluginInfo(id, version, PluginRepository.Scope.valueOf(scope)));
                }
                catch (IllegalArgumentException e)
                {
                    LOG.warning("Ignoring hash with invalid scope '" + scope + "'");
                }
            }
        }
        
        return result;
    }

    private static boolean keysValid(Hashtable<String, Object> hash)
    {
        return keyValid(hash, KEY_ID) && keyValid(hash, KEY_VERSION) && keyValid(hash, KEY_SCOPE);
    }

    private static boolean keyValid(Hashtable<String, Object> hash, String key)
    {
        Object value = hash.get(key);
        return value != null && value instanceof String;
    }

    /**
     * Returns the scope that the given plugin belongs to.
     * 
     * @param plugin the plugin to get the scope of
     * @return the scope for the plugin
     */
    public static PluginRepository.Scope getScope(Plugin plugin)
    {
        List<PluginDependency> requiredPlugins = plugin.getRequiredPlugins();
        List<PluginRepository.Scope> scopes = new ArrayList<PluginRepository.Scope>(Arrays.asList(PluginRepository.Scope.values()));
        Collections.reverse(scopes);
        for (PluginRepository.Scope scope: scopes)
        {
            for (PluginDependency dependency: requiredPlugins)
            {
                if (scope.getDependencyId().equals(dependency.getId()))
                {
                    return scope;
                }
            }
        }
        
        return scopes.get(scopes.size() - 1);
    }

    /**
     * Reads a list of plugin info from the given stream.  The content of the
     * stream is parsed as XML and interpreted using {@link #fromXML(nu.xom.Document)}.
     * 
     * @param inputStream the stream to read the XML from
     * @return a list of plugins found by reading and parsing the input stream
     * @throws IOException on error reading or parsing the stream
     */
    public static List<PluginInfo> read(InputStream inputStream) throws IOException
    {
        Document doc;
        try
        {
            Builder builder = new Builder();
            doc = builder.build(inputStream);
            return fromXML(doc);
        }
        catch (ParsingException pex)
        {
            IOException ioException = new IOException("Unable to parse available plugins:" + pex.getMessage());
            ioException.initCause(pex);
            throw ioException;
        }
    }

    /**
     * Extracts a list of plugin info from the given XML document.  Incomplete
     * and invalid entries are ignored.
     * 
     * @param document XML document to extract the info from
     * @return a list of plugin info represented in the given XML
     */
    public static List<PluginInfo> fromXML(Document document)
    {
        List<PluginInfo> result = new LinkedList<PluginInfo>();

        Element root = document.getRootElement();
        Elements pluginElements = root.getChildElements(ELEMENT_PLUGIN);
        for (int i = 0; i < pluginElements.size(); i++)
        {
            Element pluginElement = pluginElements.get(i);
            try
            {
                String id = XMLUtils.getRequiredAttributeValue(pluginElement, ATTRIBUTE_ID);
                String version = XMLUtils.getRequiredAttributeValue(pluginElement, ATTRIBUTE_VERSION);
                String categoryName = XMLUtils.getRequiredAttributeValue(pluginElement, ATTRIBUTE_SCOPE);
                PluginRepository.Scope scope = PluginRepository.Scope.valueOf(categoryName);
                result.add(new PluginInfo(id, version, scope));
            }
            catch (IllegalArgumentException e)
            {
                LOG.warning("Invalid plugin scope: " + e.getMessage(), e);
            }
            catch (XMLException e)
            {
                // Ignore incomplete entry.
            }
        }

        return result;
    }

    /**
     * Writes the given list of plugin info to the given output stream,
     * serialising it to XML first using {@link #toXML(java.util.List)}.
     * 
     * @param list         the list of info to serialise and write out
     * @param outputStream the stream to write the details to
     * @throws IOException on any error writing to the stream
     */
    public static void write(List<PluginInfo> list, OutputStream outputStream) throws IOException
    {
        Serializer serializer = new Serializer(outputStream);
        serializer.write(toXML(list));
    }

    /**
     * Converts the given list of plugin info to an XML document.
     * 
     * @param plugins the info to convert
     * @return an XML document representing the info
     */
    public static Document toXML(List<PluginInfo> plugins)
    {
        Element root = new Element(ELEMENT_PLUGINS);
        for (PluginInfo plugin: plugins)
        {
            Element pluginElement = new Element(ELEMENT_PLUGIN);
            pluginElement.addAttribute(new Attribute(ATTRIBUTE_ID, plugin.getId()));
            pluginElement.addAttribute(new Attribute(ATTRIBUTE_VERSION, plugin.getVersion()));
            pluginElement.addAttribute(new Attribute(ATTRIBUTE_SCOPE, plugin.getScope().name()));
            root.appendChild(pluginElement);
        }
        
        return new Document(root);
    }
}
