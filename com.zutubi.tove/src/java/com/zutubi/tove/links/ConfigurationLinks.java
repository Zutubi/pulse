package com.zutubi.tove.links;

import com.zutubi.i18n.Messages;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.ReflectionUtils;
import static com.zutubi.util.ReflectionUtils.acceptsParameters;
import com.zutubi.util.TextUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Holds information about links for a specific type.
 */
public class ConfigurationLinks
{
    private static final Logger LOG = Logger.getLogger(ConfigurationLinks.class);

    private static final String METHOD_NAME = "getLinks";
    private static final String KEY_SUFFIX_LABEL = ".label";

    private Class<? extends Configuration> configurationClass;
    private Object linksInstance;
    private Method linksMethod;

    /**
     * Create a links instance for the given configuration type.
     *
     * @param configurationClass class for the composite type of interest
     * @param objectFactory      used to build an instance of the *Links clas
     *                           if one exists
     */
    public ConfigurationLinks(Class<? extends Configuration> configurationClass, ObjectFactory objectFactory)
    {
        this.configurationClass = configurationClass;
        Class<?> linksClass = ConventionSupport.getLinks(configurationClass);
        if (linksClass != null)
        {
            try
            {
                linksInstance = objectFactory.buildBean(linksClass);
            }
            catch (Exception e)
            {
                LOG.severe("Unable to instantiate links class '" + linksClass + "': links will not be available for '" + configurationClass + "': " + e.getMessage(), e);
                return;
            }

            linksMethod = CollectionUtils.find(linksClass.getMethods(), new Predicate<Method>()
            {
                public boolean satisfied(Method method)
                {
                    return method.getName().equals(METHOD_NAME) &&
                            acceptsParameters(method, ConfigurationLinks.this.configurationClass) &&
                            ReflectionUtils.returnsParameterisedType(method, List.class, ConfigurationLink.class);
                }
            });
        }
    }

    /**
     * Get a list of links to display for the given instance.
     *
     * @param configuration the configuration instance, must be of our type
     * @return links for the given instance
     */
    public List<ConfigurationLink> getLinks(Configuration configuration)
    {
        if (linksMethod != null)
        {
            Messages messages = Messages.getInstance(configurationClass);
            try
            {
                @SuppressWarnings({"unchecked"})
                List<ConfigurationLink> result = (List<ConfigurationLink>) linksMethod.invoke(linksInstance, configuration);
                for (ConfigurationLink link: result)
                {
                    if (!TextUtils.stringSet(link.getLabel()))
                    {
                        link.setLabel(messages.format(link.getName() + KEY_SUFFIX_LABEL));
                    }
                }

                return result;
            }
            catch (Exception e)
            {
                LOG.severe("Resolving links for type '" + configurationClass + "': " + e.getMessage(), e);
            }
        }

        return Collections.emptyList();
    }
}
