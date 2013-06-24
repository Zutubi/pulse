package com.zutubi.tove.config;

import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;

import java.util.ArrayList;

/**
 * Base for reference walking functions that update references in place.
 * Returns a mutable record as input.
 */
abstract class ReferenceUpdatingFunction extends ReferenceWalkingFunction
{
    public ReferenceUpdatingFunction(ComplexType type, MutableRecord record, String path)
    {
        super(type, record, path);
    }

    protected void handleReferenceList(String path, Record record, TypeProperty property, String[] value)
    {
        ArrayList<String> newValue = new ArrayList<String>(value.length);
        for (String reference : value)
        {
            String newReference = updateReference(reference);
            if (newReference != null)
            {
                newValue.add(newReference);
            }
        }

        ((MutableRecord) record).put(property.getName(), newValue.toArray(new String[newValue.size()]));
    }

    protected void handleReference(String path, Record record, TypeProperty property, String value)
    {
        MutableRecord mutableRecord = (MutableRecord) record;
        String newValue = updateReference(value);
        if (newValue == null)
        {
            mutableRecord.remove(property.getName());
        }
        else
        {
            mutableRecord.put(property.getName(), newValue);
        }
    }

    protected abstract String updateReference(String value);
}
