package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.ReferenceType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.Record;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An option provider for lists that allow references to be selected.  This
 * provider lists all references of the appropriate type.
 */
public class DefaultReferenceOptionProvider extends MapOptionProvider
{
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public Map<String,String> getMap(Object instance, String path, TypeProperty property)
    {
        // We need to find all objects of a given type in a given scope...
        ReferenceType referenceType = (ReferenceType) property.getType().getTargetType();
        Map<String, Record> referencable = configurationPersistenceManager.getReferencableRecords(referenceType.getReferencedType(), path);
        Map<String, String> options = new LinkedHashMap<String, String>();

        for(Map.Entry<String, Record> entry: referencable.entrySet())
        {
            // FIXME get name properly
            options.put(entry.getKey(), (String) entry.getValue().get("name"));
        }
        
        return options;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
