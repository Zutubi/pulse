package com.zutubi.pulse.master.tove.format;

import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * Provides support for displaying state information for configuration
 * instances.
 */
public class StateDisplayManager
{
    private static final Logger LOG = Logger.getLogger(StateDisplayManager.class);

    private static final String UNABLE_TO_FORMAT = "<unable to format>";

    private Map<CompositeType, StateDisplayFields> fieldsByType = new HashMap<CompositeType, StateDisplayFields>();
    private ObjectFactory objectFactory;
    private TypeRegistry typeRegistry;

    /**
     * Returns the names of all state display fields to show for the given
     * instance.
     *
     * @param configurationInstance instance to get the fields for
     * @return names of all display fields for the instance
     */
    public List<String> getDisplayFields(Configuration configurationInstance)
    {
        if (configurationInstance != null && configurationInstance.isConcrete())
        {
            try
            {
                return getStateDisplayFields(getType(configurationInstance)).getFields(configurationInstance);
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Returns the names of all state display fields to show for the given
     * collection of instances.
     *
     * @param type           the collection's target type
     * @param collection     collection of instances to get the display fields
     *                       for
     * @param parentInstance instance that owns the collection
     * @return names of all display fields for the instance
     */
    public List<String> getCollectionDisplayFields(CompositeType type, Collection<? extends Configuration> collection, Configuration parentInstance)
    {
        if (parentInstance != null && parentInstance.isConcrete())
        {
            try
            {
                return getStateDisplayFields(type).getCollectionFields(collection, parentInstance);
            }
            catch (Exception e)
            {
                LOG.severe(e);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Formats the given display field for the given instance.
     *
     * @param fieldName             name of the field to format
     * @param configurationInstance instance to format the field for
     * @return value of the given field for the given instance
     */
    public Object format(String fieldName, Configuration configurationInstance)
    {
        CompositeType type = getType(configurationInstance);
        StateDisplayFields displayFields = getStateDisplayFields(type);
        if (displayFields.hasField(fieldName))
        {
            try
            {
                return displayFields.format(fieldName, configurationInstance);
            }
            catch (Exception e)
            {
                LOG.severe(e);
                throw new RuntimeException(e);
            }
        }
        else
        {
            LOG.warning("Request for unrecognised display field '" + fieldName + "' on path '" + configurationInstance.getConfigurationPath() + "'");
        }

        return UNABLE_TO_FORMAT;
    }

    /**
     * Formats the given display field for the given collection of instances.
     *
     * @param fieldName      name of the field to format
     * @param type           target type of the collection
     * @param collection     collection of instances to format for
     * @param parentInstance instance that owns the collection
     * @return value of the given field for the given collection of instances
     */
    public Object formatCollection(String fieldName, CompositeType type, Collection<? extends Configuration> collection, Configuration parentInstance)
    {
        StateDisplayFields displayFields = getStateDisplayFields(type);
        if (displayFields.hasCollectionField(fieldName))
        {
            try
            {
                return displayFields.formatCollection(fieldName, collection, parentInstance);
            }
            catch (Exception e)
            {
                LOG.severe(e);
                throw new RuntimeException(e);
            }
        }
        else
        {
            LOG.warning("Request for unrecognised display field '" + fieldName + "' on collection owned by path '" + parentInstance.getConfigurationPath() + "'");
        }

        return UNABLE_TO_FORMAT;
    }

    private CompositeType getType(Object configurationInstance)
    {
        CompositeType type = typeRegistry.getType(configurationInstance.getClass());
        if (type == null)
        {
            throw new IllegalArgumentException("Invalid instance: not of configuration type");
        }
        return type;
    }

    private synchronized StateDisplayFields getStateDisplayFields(CompositeType type)
    {
        StateDisplayFields fields = fieldsByType.get(type);
        if (fields == null)
        {
            Class<? extends Configuration> configurationClass = type.getClazz();
            fields = new StateDisplayFields(configurationClass, ConventionSupport.getStateDisplay(configurationClass), objectFactory);
            fieldsByType.put(type, fields);
        }

        return fields;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
