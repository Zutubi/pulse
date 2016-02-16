package com.zutubi.tove.ui.links;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Support for links from configuration interfaces to related pages.
 */
public class LinkManager
{
    private Map<CompositeType, ConfigurationLinks> linksByType = new HashMap<CompositeType, ConfigurationLinks>();

    private TypeRegistry typeRegistry;
    private ObjectFactory objectFactory;
    
    /**
     * Gets all links to display for the given configuration instance.  The
     * instance must be of a registered configuration type.
     *
     * @param configuration instance to get the links for
     * @return links to display for the instance
     */
    public synchronized List<ConfigurationLink> getLinks(Configuration configuration)
    {
        CompositeType type = typeRegistry.getType(configuration.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Attempt to get links for configuration of unregistered type '" + configuration.getClass() + "'");
        }

        return getLinks(type).getLinks(configuration);
    }

    private ConfigurationLinks getLinks(CompositeType type)
    {
        ConfigurationLinks links = linksByType.get(type);
        if (links == null)
        {
            links = new ConfigurationLinks(type.getClazz(), objectFactory);
            linksByType.put(type, links);
        }
        return links;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
