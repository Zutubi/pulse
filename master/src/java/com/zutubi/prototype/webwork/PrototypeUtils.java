package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.util.StringUtils;

import java.util.Collection;
import java.util.Map;

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
        MutableRecord record = type.createNewRecord();

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
}
