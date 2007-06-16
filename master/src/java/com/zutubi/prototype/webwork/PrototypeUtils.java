package com.zutubi.prototype.webwork;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.util.StringUtils;

import java.util.*;

/**
 * Webwork environment specific prototype utility methods.
 *
 */
public class PrototypeUtils
{
    public static String getConfigURL(String path, String action, String submitField)
    {
        return getConfigURL(path, action, submitField, false);
    }

    public static String getConfigURL(String path, String action, String submitField, boolean ajax)
    {
        String result;

        if (ajax)
        {
            result = ConfigurationActionMapper.AJAX_CONFIG_NAMESPACE;
        }
        else
        {
            result = ConfigurationActionMapper.CONFIG_NAMESPACE;
        }

        if(path != null)
        {
            result = StringUtils.join("/", true, true, result, path);
        }

        result = PathUtils.normalizePath(result);

        if(action != null && !action.equals("display") || submitField != null)
        {
            result = result + "?" + action;
        }

        if(submitField != null)
        {
            result = result + "=" + submitField;
        }

        return result;
    }

    /**
     * Convert the parameters from the HTTP post into a record, according to the type definition.
     * <p/>
     * NOTE: This method does not do any real type conversion. Instead, it 'unwraps' data that has been
     * wrapped in String[]s'.
     *
     * @param type       instance that defines the data contained in the parameters map.
     * @param parameters map that contains the http parameters to be converted into a record.
     * @return a record instance containing the parameter data that applies to the map.
     */
    public static Record toRecord(CompositeType type, Map<String, String[]> parameters)
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName(type.getSymbolicName());

        for (TypeProperty property : type.getProperties())
        {
            String propertyName = property.getName();

            // this is a read only property.
            if (!property.isWriteable())
            {
                continue;
            }

            String[] parameterValue = parameters.get(propertyName);
            if(parameterValue == null)
            {
                parameterValue = parameters.get(propertyName + ".default");
                if(parameterValue == null)
                {
                    continue;
                }
            }

            if (Collection.class.isAssignableFrom(property.getClazz()))
            {
                record.put(propertyName, parameterValue);
            }
            else
            {
                record.put(propertyName, parameterValue[0]);
            }
        }
        return record;
    }

    public static boolean isFolder(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        Type type = configurationTemplateManager.getType(path);
        return (type instanceof CollectionType) || getPathListing(path, type, configurationTemplateManager).size() > 0;
    }

    public static boolean isLeaf(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        return !isFolder(path, configurationTemplateManager);
    }

    @SuppressWarnings({"unchecked"})
    public static List<String> getPathListing(String path, Type type, ConfigurationTemplateManager configurationTemplateManager)
    {
        // FIXME: this listing should be the I18N names, not the property names. However,
        //        the listing in the UI needs to be modified to support separate display
        //        names and paths. IE, the display name will be the I18N string, and the
        //        path will be the properties actual name.

        List<String> listing = Collections.EMPTY_LIST;

        if(path.length() == 0)
        {
            listing = configurationTemplateManager.getRootListing();
        }
        else if(type instanceof CollectionType)
        {
            Record record = configurationTemplateManager.getRecord(path);
            if(record != null)
            {
                listing = new LinkedList<String>(record.keySet());
            }
        }
        else if(type instanceof CompositeType)
        {
            listing = getNestedProperties((CompositeType) type);
        }

        return listing;
    }

    public static List<String> getSimpleProperties(CompositeType ctype)
    {
        List<String> listing = new LinkedList<String>();
        for (String propertyName : ctype.getPropertyNames(PrimitiveType.class))
        {
            listing.add(propertyName);
        }

        for (TypeProperty property: ctype.getProperties(CollectionType.class))
        {
            final CollectionType propertyType = (CollectionType) property.getType();
            if(propertyType.getCollectionType() instanceof SimpleType)
            {
                listing.add(property.getName());
            }
        }

        return listing;
    }

    public static List<String> getNestedProperties(CompositeType ctype)
    {
        List<String> listing = new LinkedList<String>();
        for (String propertyName : ctype.getPropertyNames(CompositeType.class))
        {
            listing.add(propertyName);
        }

        for (TypeProperty property: ctype.getProperties(CollectionType.class))
        {
            final CollectionType propertyType = (CollectionType) property.getType();
            if(!(propertyType.getCollectionType() instanceof SimpleType))
            {
                listing.add(property.getName());
            }
        }
        return listing;
    }
}
