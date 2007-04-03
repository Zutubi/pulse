package com.zutubi.prototype.webwork;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.MutableRecord;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * Webwork environment specific prototype utility methods.
 *
 */
public class PrototypeUtils
{
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

            // no data is available for this particular property.
            if (!parameters.containsKey(propertyName))
            {
                continue;
            }

            // this is a read only property.
            if (!property.isWritable())
            {
                continue;
            }

            String[] parameterValue = parameters.get(propertyName);
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
