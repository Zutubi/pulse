package com.zutubi.prototype.config;

import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.record.*;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.MessagesTextProvider;

/**
 * Provides high-level refactoring actions for configuration.
 */
public class ConfigurationRefactoringManager
{
    private ConfigurationTemplateManager configurationTemplateManager;

    public String clone(String path, String cloneKey, boolean cloneTemplateDescendents) throws ValidationException
    {
        // Validate new name, get a clone of the instance, set new key, insert?
        String parentPath = PathUtils.getParentPath(path);
        if(parentPath == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
        }

        Record record = configurationTemplateManager.getRecord(path);
        if(record == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': path does not exist");            
        }

        MapType parentType = configurationTemplateManager.getType(parentPath, MapType.class);
        CompositeType type = parentType.getTargetType();
        String keyPropertyName = parentType.getKeyProperty();
        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());

        if(!TextUtils.stringSet(cloneKey))
        {
            throw new ValidationException(textProvider.getText(".required", keyPropertyName + " is required", keyPropertyName));
        }

        configurationTemplateManager.validateNameIsUnique(parentPath, cloneKey, keyPropertyName, textProvider);

        if(record instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) record;
            if(cloneTemplateDescendents)
            {
//                configurationTemplateManager.getDescendentPaths(path, true, false, );
            }

            record = templateRecord.getMoi();
        }

        MutableRecord clone = record.copy(true);
        clearHandles(clone);
        clone.put(keyPropertyName, cloneKey);

        return configurationTemplateManager.insertRecord(parentPath, clone);
    }

    private void clearHandles(MutableRecord record)
    {
        record.setHandle(RecordManager.UNDEFINED);
        for(String key: record.nestedKeySet())
        {
            clearHandles((MutableRecord) record.get(key));
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
