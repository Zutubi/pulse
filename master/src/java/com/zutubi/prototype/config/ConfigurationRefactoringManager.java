package com.zutubi.prototype.config;

import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import static com.zutubi.util.CollectionUtils.makeHashMap;
import static com.zutubi.util.CollectionUtils.makePair;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides high-level refactoring actions for configuration.
 */
public class ConfigurationRefactoringManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRefactoringManager.class);

    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;

    public void clone(final String parentPath, final Map<String, String> oldKeyToNewKey)
    {
        // Be careful that no two new keys conflict!
        // Be careful that references (including parent pointers) within the
        // clone set are properly updated to point to clones.
        configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<Object>()
        {
            public Object execute() throws Exception
            {
                // First let us validate all aspects
                ComplexType parentType = configurationTemplateManager.getType(parentPath);
                if (!(parentType instanceof MapType))
                {
                    throw new IllegalArgumentException("Invalid parent path '" + parentPath + "': only elements of a map collection may be cloned (parent has type " + parentType.getClass().getName() + ")");
                }

                MapType mapType = (MapType) parentType;
                String keyPropertyName = mapType.getKeyProperty();
                MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());

                // Copy all the records we want to clone
                Set<String> seenNames = new HashSet<String>();
                for (Map.Entry<String, String> entry : oldKeyToNewKey.entrySet())
                {
                    String originalKey = entry.getKey();
                    String cloneKey = entry.getValue();

                    if (!TextUtils.stringSet(originalKey))
                    {
                        throw new IllegalArgumentException("Invalid empty original key");
                    }

                    if (!TextUtils.stringSet(cloneKey))
                    {
                        throw new IllegalArgumentException("Invalid empty clone key");
                    }

                    if (seenNames.contains(cloneKey))
                    {
                        throw new IllegalArgumentException("Duplicate key '" + cloneKey + "' found: all new clones must have unique keys");
                    }

                    String path = PathUtils.getPath(parentPath, originalKey);
                    Record record = configurationTemplateManager.getRecord(path);
                    if (record == null)
                    {
                        throw new IllegalArgumentException("Invalid path '" + path + "': path does not exist");
                    }

                    seenNames.add(cloneKey);
                    configurationTemplateManager.validateNameIsUnique(parentPath, cloneKey, keyPropertyName, textProvider);

                    if (record instanceof TemplateRecord)
                    {
                        record = ((TemplateRecord) record).getMoi();
                    }

                    MutableRecord clone = record.copy(true);
                    clearHandles(clone);
                    clone.put(keyPropertyName, cloneKey);
                    configurationTemplateManager.insertRecord(parentPath, clone);
                }

                // Now rewrite all internal and parent references that fall inside the clone set
                rewriteReferences(parentPath, oldKeyToNewKey);
                return null;
            }
        });
    }

    private void rewriteReferences(String parentPath, Map<String, String> oldKeyToNewKey)
    {
        for (Map.Entry<String, String> entry : oldKeyToNewKey.entrySet())
        {
            String path = PathUtils.getPath(parentPath, entry.getValue());
            Record record = configurationTemplateManager.getRecord(path);
            if (record instanceof TemplateRecord)
            {
                record = ((TemplateRecord) record).getMoi();
            }

            rewriteRecordReferences(path, record, parentPath, oldKeyToNewKey);
        }
    }

    private void rewriteRecordReferences(String path, Record record, String parentPath, Map<String, String> oldKeyToNewKey)
    {
        String symbolicName = record.getSymbolicName();
        if (symbolicName != null)
        {
            CompositeType type = typeRegistry.getType(symbolicName);
            MutableRecord mutableRecord = null;
            for (TypeProperty property : type.getProperties(ReferenceType.class))
            {
                Object data = record.get(property.getName());
                if (data != null)
                {
                    try
                    {
                        // Does the referenced path fall into the cloneset?
                        final ReferenceType referenceType = (ReferenceType) property.getType();
                        String referencedPath = referenceType.getReferencedPath(data);
                        if (inCloneSet(referencedPath, parentPath, oldKeyToNewKey.keySet()))
                        {
                            // Then we need to update the reference to point to
                            // the new clone.
                            String newPath = convertPath(referencedPath, parentPath, oldKeyToNewKey);
                            long newHandle = configurationReferenceManager.getHandleForPath(newPath);
                            if (mutableRecord == null)
                            {
                                mutableRecord = record.copy(false);
                            }

                            mutableRecord.put(property.getName(), Long.toString(newHandle));
                        }
                    }
                    catch (TypeException e)
                    {
                        LOG.severe(e);
                    }
                }
            }

            if (mutableRecord != null)
            {
                configurationTemplateManager.saveRecord(path, mutableRecord);
            }
        }

        for (String key : record.nestedKeySet())
        {
            rewriteRecordReferences(PathUtils.getPath(path, key), (Record) record.get(key), parentPath, oldKeyToNewKey);
        }
    }

    private boolean inCloneSet(String path, String parentPath, Set<String> oldKeys)
    {
        if (path.startsWith(parentPath))
        {
            path = path.substring(parentPath.length());
            String[] keyRest = StringUtils.getNextToken(path, PathUtils.SEPARATOR_CHAR, true);
            return oldKeys.contains(keyRest[0]);
        }

        return false;
    }

    private String convertPath(String path, String parentPath, Map<String, String> oldKeyToNewKey)
    {
        path = path.substring(parentPath.length());
        String[] keyRest = StringUtils.getNextToken(path, PathUtils.SEPARATOR_CHAR, true);
        return PathUtils.getPath(parentPath, oldKeyToNewKey.get(keyRest[0]), keyRest[1]);
    }

    public String clone(String path, String cloneKey)
    {
        // Validate new name, get a clone of the instance, set new key, insert?
        String parentPath = PathUtils.getParentPath(path);
        if (parentPath == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
        }

        clone(parentPath, makeHashMap(Arrays.asList(makePair(PathUtils.getBaseName(path), cloneKey))));
        return PathUtils.getPath(parentPath, cloneKey);

        //        Record record = configurationTemplateManager.getRecord(path);
        //        if(record == null)
        //        {
        //            throw new IllegalArgumentException("Invalid path '" + path + "': path does not exist");
        //        }
        //
        //        ComplexType parentType = configurationTemplateManager.getType(parentPath);
        //        if(!(parentType instanceof MapType))
        //        {
        //            throw new IllegalArgumentException("Invalid path '" + path + "': only elements of a map collection may be cloned (parent has type " + parentType.getClass().getName() + ")");
        //        }
        //
        //        MapType mapType = (MapType) parentType;
        //        CompositeType type = mapType.getTargetType();
        //        String keyPropertyName = mapType.getKeyProperty();
        //        MessagesTextProvider textProvider = new MessagesTextProvider(type.getClazz());
        //
        //        if(!TextUtils.stringSet(cloneKey))
        //        {
        //            throw new ValidationException(textProvider.getText(".required", keyPropertyName + " is required", keyPropertyName));
        //        }
        //
        //        configurationTemplateManager.validateNameIsUnique(parentPath, cloneKey, keyPropertyName, textProvider);
        //
        //        if(record instanceof TemplateRecord)
        //        {
        //            record = ((TemplateRecord) record).getMoi();
        //        }
        //
        //        MutableRecord clone = record.copy(true);
        //        clearHandles(clone);
        //        clone.put(keyPropertyName, cloneKey);
        //
        //        return configurationTemplateManager.insertRecord(parentPath, clone);
    }

    private void clearHandles(MutableRecord record)
    {
        record.setHandle(RecordManager.UNDEFINED);
        for (String key : record.nestedKeySet())
        {
            clearHandles((MutableRecord) record.get(key));
        }
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }
}
