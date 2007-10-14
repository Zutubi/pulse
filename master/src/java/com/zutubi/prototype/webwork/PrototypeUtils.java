package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ValidationAware;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.webwork.mapping.PulseActionMapper;
import com.zutubi.util.StringUtils;

import java.util.*;

/**
 * Webwork environment specific prototype utility methods.
 */
public class PrototypeUtils
{
    private static final String KEY_LABEL = "label";
    private static final String KEY_FORM_HEADING = "form.heading";
    private static final String KEY_TABLE_HEADING = "table.heading";

    private static final String[] EMPTY_ARRAY = {};

    public static String getConfigURL(String path, String action, String submitField)
    {
        return getConfigURL(path, action, submitField, null);
    }

    public static String getConfigURL(String path, String action, String submitField, String namespace)
    {
        String result = (namespace != null) ? namespace : PulseActionMapper.ADMIN_NAMESPACE;
        if (path != null)
        {
            result = StringUtils.join("/", true, true, result, path);
        }

        result = PathUtils.normalizePath(result);
        if (action != null && !action.equals("display") || submitField != null)
        {
            result = result + "?" + action;
        }

        if (submitField != null)
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
            if (parameterValue == null)
            {
                parameterValue = parameters.get(propertyName + ".default");
                if (parameterValue == null)
                {
                    continue;
                }
            }

            if (Collection.class.isAssignableFrom(property.getClazz()))
            {
                if(parameterValue.length == 1 && parameterValue[0].length() == 0)
                {
                    // This indicates an empty array: something the UI is
                    // incapable of sending directly for custom components.
                    parameterValue = EMPTY_ARRAY;
                }
                record.put(propertyName, parameterValue);
            }
            else
            {
                record.put(propertyName, parameterValue[0]);
            }
        }
        return record;
    }

    public static boolean isFolder(String path, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        Type type = configurationTemplateManager.getType(path);
        return (type instanceof MapType) || getPathListing(path, type, configurationTemplateManager, configurationSecurityManager).size() > 0;
    }

    public static boolean isLeaf(String path, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        return !isFolder(path, configurationTemplateManager, configurationSecurityManager);
    }

    @SuppressWarnings({"unchecked"})
    public static List<String> getPathListing(String path, Type type, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        List<String> listing = Collections.EMPTY_LIST;

        if (path.length() == 0)
        {
            listing = configurationTemplateManager.getRootListing();
        }
        else if (type instanceof MapType)
        {
            Record record = configurationTemplateManager.getRecord(path);
            if (record != null)
            {
                listing = new LinkedList<String>(((CollectionType) type).getOrder(record));
            }
        }
        else if (type instanceof CompositeType)
        {
            listing = ((CompositeType)type).getNestedPropertyNames();
        }

        return configurationSecurityManager.filterPaths(path, listing, AccessManager.ACTION_VIEW);
    }

    public static List<String> getEmbeddedCollections(CompositeType ctype)
    {
        List<String> result = new LinkedList<String>();
        for(TypeProperty property: ctype.getProperties())
        {
            if(isEmbeddedCollection(property.getType()))
            {
                result.add(property.getName());
            }
        }

        return result;
    }

    public static boolean isEmbeddedCollection(Type type)
    {
        return type instanceof ListType && type.getTargetType() instanceof ComplexType;
    }

    public static String getDisplayName(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        if (TextUtils.stringSet(path))
        {
            String parentPath = PathUtils.getParentPath(path);
            if (parentPath == null)
            {
                // A scope, just return the scope name;
                return path;
            }
            else
            {
                if (configurationTemplateManager.isPersistent(path))
                {
                    return getDisplayName(path, configurationTemplateManager.getType(parentPath), configurationTemplateManager.getRecord(path));
                }
                else
                {
                    ComplexType type = configurationTemplateManager.getType(path);
                    Messages messages = Messages.getInstance(type.getClazz());
                    if(messages.isKeyDefined("label"))
                    {
                        return messages.format("label");
                    }
                    else
                    {
                        return PathUtils.getBaseName(path);
                    }
                }
            }
        }
        else
        {
            // Empty path, empty name
            return path;
        }
    }

    public static String getDisplayName(String path, ComplexType parentType, Record value)
    {
        // One of:
        //   - the id, if this object is within a map
        //   - the value of the first defined i18n key if this is a composite
        //     property:
        //       <parent type>.properties: <property>.label
        //       <property type>.properties: label.plural (if a collection)
        //       <property type>.properties: label (auto-pluralised if a collection)
        String result = null;
        String baseName = PathUtils.getBaseName(path);

        if (parentType != null)
        {
            if (parentType instanceof MapType)
            {
                result = (String) value.get(((MapType) parentType).getKeyProperty());
            }
            else
            {
                Messages messages = Messages.getInstance(parentType.getClazz());
                String key = baseName + ".label";
                if (messages.isKeyDefined(key))
                {
                    result = messages.format(key);
                }
                else
                {
                    Type declaredType = parentType.getDeclaredPropertyType(baseName);
                    if (declaredType instanceof CollectionType)
                    {
                        result = getPluralLabel(declaredType.getTargetType());
                    }
                    else
                    {
                        messages = Messages.getInstance(declaredType.getClazz());
                        result = messages.format("label");
                    }
                }
            }
        }

        if (result == null)
        {
            result = baseName;
        }

        return result;
    }

    public static String getFormHeading(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        if(messages.isKeyDefined(KEY_FORM_HEADING))
        {
            return messages.format(KEY_FORM_HEADING);
        }
        else
        {
            // Default is just the label.
            return messages.format(KEY_LABEL);
        }
    }

    public static String getTableHeading(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        if(messages.isKeyDefined(KEY_TABLE_HEADING))
        {
            return messages.format(KEY_TABLE_HEADING);
        }
        else
        {
            return getPluralLabel(messages);
        }
    }

    public static String getPluralLabel(Type type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        return getPluralLabel(messages);
    }

    private static String getPluralLabel(Messages messages)
    {
        if (messages.isKeyDefined("label.plural"))
        {
            return messages.format("label.plural");
        }
        else
        {
            // Auto-pluralise
            return StringUtils.pluralise(messages.format("label"));
        }
    }

    public static void mapErrors(Configuration instance, ValidationAware validationAware, String fieldSuffix)
    {
        for(String instanceError: instance.getInstanceErrors())
        {
            validationAware.addActionError(instanceError);
        }

        for(Map.Entry<String, List<String>> fieldEntry: instance.getFieldErrors().entrySet())
        {
            String fieldName = fieldSuffix == null ? fieldEntry.getKey() : fieldEntry.getKey() + fieldSuffix;
            for(String error: fieldEntry.getValue())
            {
                validationAware.addFieldError(fieldName, error);
            }
        }
    }
}
