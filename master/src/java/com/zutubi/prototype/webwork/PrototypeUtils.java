package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
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
    public static MutableRecord toRecord(CompositeType type, Map<String, String[]> parameters)
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
                listing = new LinkedList<String>(((CollectionType)type).getOrder(record));
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

    public static String getDisplayName(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        if(TextUtils.stringSet(path))
        {
            String parentPath = PathUtils.getParentPath(path);
            if(parentPath == null)
            {
                // A scope, just return the scope name;
                return path;
            }
            else
            {
                return getDisplayName(path, configurationTemplateManager.getType(parentPath), configurationTemplateManager.getRecord(path), configurationTemplateManager);
            }
        }
        else
        {
            // Empty path, empty name
            return path;
        }
    }
    
    public static String getDisplayName(String path, ComplexType parentType, Record value, ConfigurationTemplateManager configurationTemplateManager)
    {
        // One of:
        //   - the id, if this object is within a map
        //   - toString representation if this object is in a list
        //   - the value of the first defined i18n key if this is composite
        //     a property:
        //       <parent type>.properties: <property>.property.label
        //       <property type>.properties: label.plural (if a collection)
        //       <property type>.properties: label (auto-pluralised if a collection)
        String result = null;
        String baseName = PathUtils.getBaseName(path);

        if(parentType != null)
        {
            if(parentType instanceof MapType)
            {
                result = (String) value.get(((MapType) parentType).getKeyProperty());
            }
            else if(parentType instanceof ListType)
            {
                Object instance = configurationTemplateManager.getInstance(path);
                if(instance != null)
                {
                    result = instance.toString();
                }
            }
            else
            {
                Messages messages = Messages.getInstance(parentType.getClazz());
                String key = baseName + ".label";
                if(messages.isKeyDefined(key))
                {
                    result = messages.format(key);
                }
                else
                {
                    Type declaredType = parentType.getDeclaredPropertyType(baseName);
                    if(declaredType instanceof CollectionType)
                    {
                        messages = Messages.getInstance(declaredType.getTargetType().getClazz());
                        if(messages.isKeyDefined("label.plural"))
                        {
                            result = messages.format("label.plural");
                        }
                        else
                        {
                            // Auto-pluralise
                            result = StringUtils.pluralise(messages.format("label"));
                        }
                    }
                    else
                    {
                        messages = Messages.getInstance(declaredType.getClazz());
                        result = messages.format("label");
                    }
                }
            }
        }

        if(result == null)
        {
            result = baseName;
        }

        return result;
    }
}
