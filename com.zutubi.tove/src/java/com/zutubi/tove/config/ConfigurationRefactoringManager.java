package com.zutubi.tove.config;

import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.CollectionUtils;
import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.Pair;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.*;

/**
 * Provides high-level refactoring actions for configuration.
 */
public class ConfigurationRefactoringManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRefactoringManager.class);

    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private RecordManager recordManager;

    /**
     * Tests if the given path is cloneable.  This does <b>not</b> take into
     * account security: i.e. it does not check that the user can insert into
     * the parent path.
     *
     * @param path the path to test, which much exist
     * @return true iff the path can be cloned; it must be a map item and not
     *         permananet or the root of a template hierarchy
     */
    public boolean canClone(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                String parentPath = PathUtils.getParentPath(path);
                if (parentPath != null && configurationTemplateManager.pathExists(path))
                {
                    Record record = configurationTemplateManager.getRecord(path);
                    if (!record.isPermanent())
                    {
                        Type parentType = configurationTemplateManager.getType(parentPath);
                        if (parentType instanceof MapType)
                        {
                            return !configurationTemplateManager.isTemplatedCollection(parentPath) || configurationTemplateManager.getTemplateParentRecord(path) != null;
                        }
                    }
                }

                return false;
            }
        });
    }

    /**
     * <p>
     * Clones elements of a map, producing exact replicas with the exception
     * of the keys which are changed.  The map can be a top-level map
     * (including templated collections) or a map nested anywhere in a
     * persistent scope.  The clone operation performs similarly in both
     * cases with only the parent references in templates being treated
     * specially (see below).
     * </p>
     * <p>
     * Note that this method allows multiple elements of the same map to be
     * cloned in a single operation.  Multiple elements should be cloned
     * together when it is desirable to update references between them
     * (including parent references) to point to the new clones.  For
     * example, take two items map/a and map/b.  Say path map/a/ref is a
     * reference that points to map/b/foo.  If map/a and map/b are cloned
     * separately to form map/clonea and map/cloneb, then map/clonea/ref will
     * continue to point to map/b/foo.  If these two operations were done in
     * a single call to this method, however, then map/clonea/ref will point
     * to map/cloneb/foo.  Similarly, to clone a parent and child in a
     * template hierarchy and have the cloned parent be the parent of the
     * cloned child, they must be cloned in a single operation.  A member of
     * a template collection that is cloned without its parent being involved
     * in the same operation will result in a clone that has the original
     * parent.
     * </p>
     * <p>
     * Each original key must refer to an existing item in the map.
     * </p>
     * <p>
     * Each new clone key must be unique in its template hierarchy and also
     * in the map itself.  For this reason no duplicate new keys are allowed.
     * </p>
     * <p>
     * The root of a template hierarchy cannot be cloned as each hierarchy
     * can only have one root.
     * <p>
     *
     * @see #canClone(String)
     *
     * @param parentPath            path of the map to clone elements of
     * @param originalKeyToCloneKey map from original keys (denoting the
     *                              elements to clone) to clone keys (the new
     *                              key for each clone)
     * @throws IllegalArgumentException if a given path or key is invalid
     */
    public void clone(String parentPath, Map<String, String> originalKeyToCloneKey)
    {
        configurationTemplateManager.executeInsideTransaction(new CloneAction(parentPath, originalKeyToCloneKey));
    }

    /**
     * Clones an element of a map, producing an exact replica with the
     * exception of a new key.  If the path points to an element of a
     * templated collection, the new clone will have the same parent as the
     * original element.
     *
     * @see #clone(String, java.util.Map)
     *
     * @param path     path of the element to clone: must be in a map
     * @param cloneKey new key to give to the clone
     * @return the path of the new clone
     * @throws IllegalArgumentException if the given path or key is invalid
     */
    public String clone(final String path, final String cloneKey)
    {
        // Validate new name, get a clone of the instance, set new key, insert?
        return configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<String>()
        {
            public String execute() throws Exception
            {
                String parentPath = PathUtils.getParentPath(path);
                if (parentPath == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
                }

                ConfigurationRefactoringManager.this.clone(parentPath, asMap(asPair(PathUtils.getBaseName(path), cloneKey)));
                return getPath(parentPath, cloneKey);
            }
        });
    }

    /**
     * Tests if the given path can have a template parent extracted from it.
     * Parents can be extracted from items of templated collections,
     * excepting the root of the template tree.
     *
     * @see #extractParentTemplate(String, java.util.List, String)
     *
     * @param path the path to test
     * @return true iff a parent template may be extracted from the given
     *         path
     */
    public boolean canExtractParentTemplate(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                return configurationTemplateManager.pathExists(path) && configurationTemplateManager.isTemplatedCollection(PathUtils.getParentPath(path)) && configurationTemplateManager.getTemplateParentRecord(path) != null;
            }
        });
    }

    /**
     * Extracts the common values from a set of siblings into a parent
     * template.  After the operation is complete, the final values of all
     * fields (in the deep sense) of each sibling is unchanged, but any value
     * that is identical in all siblings is now inherited from the new
     * template parent.
     *
     * @param parentPath         path of the templated collection that the
     *                           siblings are items of
     * @param keys               keys of the sibling items to extract the
     *                           parent from (all keys must refer to items
     *                           with the same template parent)
     * @param parentTemplateKey  key for the new template parent
     * @return the path of the new template parent
     * @throws IllegalArgumentException if the the parent path is not a
     *         templated collection; if no keys are specified; if the keys do
     *         not share a common, non-null template parent; if the parent
     *         template name is invalid or in use
     */
    public String extractParentTemplate(final String parentPath, final List<String> keys, final String parentTemplateKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new ExtractParentTemplateAction(parentPath, parentTemplateKey, keys));
    }

    /**
     * Tests if the given path may be 'smart' cloned.  As a smart clone is
     * performed by extracting a parent template and then cloning, a path can
     * be smart cloned if both of these operations are valid on the path.
     *
     * @see #smartClone(String, String, String, java.util.Map)
     *
     * @param path the path to test
     * @return true iff the given path can be smart cloned
     */
    public boolean canSmartClone(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new ConfigurationTemplateManager.Action<Boolean>()
        {
            public Boolean execute() throws Exception
            {
                return canExtractParentTemplate(path) && canClone(path);
            }
        });
    }

    /**
     * Clones a templated map item by extracting its details into a parent
     * template and adding a skeletal sibling (the clone).  The item's
     * descendents may also optionally be cloned, although those clones
     * themselves will not be 'smart'.
     *
     * @param parentPath            path to the templated collection that
     *                              owns the item to clone
     * @param rootKey               key of the item to smart clone
     * @param parentKey             key to give to the newly-extracted parent
     *                              template
     * @param originalKeyToCloneKey a mapping from existing key to new key
     *                              for all items to clone (must include at
     *                              least a mapping for rootKey)
     * @return the path of the smart clone
     * @throws IllegalArgumentException if the parentPath is not a templated
     *         collection; rootKey is not a member of the collection;
     *         parentKey or any clone key is not unique in the collection;
     *         originalKeyToCloneKey does not contain a mapping for rootKey
     */
    public String smartClone(String parentPath, String rootKey, String parentKey, Map<String, String> originalKeyToCloneKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new SmartCloneAction(parentPath, rootKey, parentKey, originalKeyToCloneKey));
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

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    /**
     * Action used to implement {@link ConfigurationRefactoringManager#clone(String, java.util.Map)}.
     */
    private class CloneAction implements ConfigurationTemplateManager.Action<Object>
    {
        private final String parentPath;
        private final Map<String, String> originalKeyToCloneKey;

        public CloneAction(String parentPath, Map<String, String> originalKeyToCloneKey)
        {
            this.parentPath = parentPath;
            this.originalKeyToCloneKey = originalKeyToCloneKey;
        }

        public Object execute() throws Exception
        {
            // Be careful that no two new keys conflict!
            // Be careful that references (including parent pointers) within the
            // clone set are properly updated to point to clones.
            ComplexType parentType = configurationTemplateManager.getType(parentPath);
            if (!(parentType instanceof MapType))
            {
                throw new IllegalArgumentException("Invalid parent path '" + parentPath + "': only elements of a map collection may be cloned (parent has type " + parentType.getClass().getName() + ")");
            }

            MapType mapType = (MapType) parentType;
            String keyPropertyName = mapType.getKeyProperty();
            MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());

            boolean templatedCollection = configurationTemplateManager.isTemplatedCollection(parentPath);
            Collection<String> originalKeys;
            if(templatedCollection)
            {
                // Sort the keys in order of template depth so we can be
                // sure that a parent is cloned before its children.
                originalKeys = sortKeys(parentPath, originalKeyToCloneKey);
            }
            else
            {
                originalKeys = originalKeyToCloneKey.keySet();
            }

            // Copy each record, updating parent references if necessary as
            // we go (reparenting is not safe due to scrubbing).
            Set<String> seenNames = new HashSet<String>();
            for (String originalKey: originalKeys)
            {
                String cloneKey = originalKeyToCloneKey.get(originalKey);
                Record record = checkKeys(parentPath, originalKey, cloneKey, templatedCollection, seenNames);
                configurationTemplateManager.validateNameIsUnique(parentPath, cloneKey, keyPropertyName, textProvider);

                MutableRecord clone;
                if (record instanceof TemplateRecord)
                {
                    clone = ((TemplateRecord) record).flatten();
                }
                else
                {
                    clone = record.copy(true);
                }
                
                clearHandles(clone);
                clone.put(keyPropertyName, cloneKey);

                if(templatedCollection)
                {
                    checkParent(parentPath, originalKey, clone, originalKeyToCloneKey);
                }

                configurationTemplateManager.insertRecord(parentPath, clone);
            }

            // Now rewrite all internal references that fall inside the clone set
            rewriteReferences(parentPath, originalKeyToCloneKey);
            return null;
        }

        private Collection<String> sortKeys(String scope, Map<String, String> originalKeyToCloneKey)
        {
            final TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scope);
            List<String> result = new LinkedList<String>(originalKeyToCloneKey.keySet());
            Collections.sort(result, new Comparator<String>()
            {
                public int compare(String key1, String key2)
                {
                    return getDepth(hierarchy.getNodeById(key1)) - getDepth(hierarchy.getNodeById(key2));
                }

                private int getDepth(TemplateNode node)
                {
                    return node == null ? 0 : node.getDepth();
                }
            });

            return result;
        }

        private Record checkKeys(String parentPath, String originalKey, String cloneKey, boolean templatedCollection, Set<String> seenNames)
        {
            if (!StringUtils.stringSet(originalKey))
            {
                throw new IllegalArgumentException("Invalid empty original key");
            }

            if (!StringUtils.stringSet(cloneKey))
            {
                throw new IllegalArgumentException("Invalid empty clone key");
            }

            if (seenNames.contains(cloneKey))
            {
                throw new IllegalArgumentException("Duplicate key '" + cloneKey + "' found: all new clones must have unique keys");
            }

            String path = getPath(parentPath, originalKey);
            Record record = configurationTemplateManager.getRecord(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': path does not exist");
            }

            if (record.isPermanent())
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': refers to a permanent record");
            }
            
            if(templatedCollection && configurationTemplateManager.getTemplateParentHandle(path, record) == 0)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': cannot clone root of a template hierarchy");
            }

            seenNames.add(cloneKey);

            return record;
        }

        private void clearHandles(MutableRecord record)
        {
            record.setHandle(RecordManager.UNDEFINED);
            for (String key : record.nestedKeySet())
            {
                clearHandles((MutableRecord) record.get(key));
            }
        }

        private void checkParent(String scope, String originalKey, MutableRecord clone, Map<String, String> originalKeyToCloneKey)
        {
            TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scope);
            TemplateNode node = hierarchy.getNodeById(originalKey);
            String parentCloneKey = originalKeyToCloneKey.get(node.getParent().getId());
            if(parentCloneKey != null)
            {
                // Parent is also in the clone set: update the parent reference
                configurationTemplateManager.setParentTemplate(clone, configurationTemplateManager.getRecord(getPath(scope, parentCloneKey)).getHandle());
            }
        }

        private void rewriteReferences(String parentPath, Map<String, String> oldKeyToNewKey)
        {
            for (Map.Entry<String, String> entry : oldKeyToNewKey.entrySet())
            {
                String path = getPath(parentPath, entry.getValue());
                Record record = configurationTemplateManager.getRecord(path);
                String templateOwnerPath;
                if (record instanceof TemplateRecord)
                {
                    record = ((TemplateRecord) record).getMoi();
                    templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(PathUtils.getPath(parentPath, entry.getKey()));
                }
                else
                {
                    templateOwnerPath = null;
                }

                rewriteRecordReferences(path, record, parentPath, templateOwnerPath, oldKeyToNewKey);
            }
        }

        private void rewriteRecordReferences(String path, Record record, String parentPath, String templateOwnerPath, Map<String, String> oldKeyToNewKey)
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
                            // Does the referenced path fall into the clone set?
                            final ReferenceType referenceType = (ReferenceType) property.getType();
                            String referencedPath = referenceType.getReferencedPath(templateOwnerPath, data);
                            if (inCloneSet(referencedPath, parentPath, oldKeyToNewKey.keySet()))
                            {
                                // Then we need to update the reference to point to
                                // the new clone.
                                String newPath = convertPath(referencedPath, parentPath, oldKeyToNewKey);
                                long newHandle = configurationReferenceManager.getReferenceHandleForPath(newPath);
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
                rewriteRecordReferences(getPath(path, key), (Record) record.get(key), parentPath, templateOwnerPath, oldKeyToNewKey);
            }
        }

        private boolean inCloneSet(String path, String parentPath, Set<String> oldKeys)
        {
            if (path != null && path.startsWith(parentPath))
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
            return getPath(parentPath, oldKeyToNewKey.get(keyRest[0]), keyRest[1]);
        }
    }

    private class SmartCloneAction implements ConfigurationTemplateManager.Action<String>
    {
        private final String parentPath;
        private final String rootKey;
        private final String parentKey;
        private final Map<String, String> originalKeyToCloneKey;

        public SmartCloneAction(String parentPath, String rootKey, String parentKey, Map<String, String> originalKeyToCloneKey)
        {
            this.parentPath = parentPath;
            this.rootKey = rootKey;
            this.parentKey = parentKey;
            this.originalKeyToCloneKey = originalKeyToCloneKey;
        }

        public String execute() throws Exception
        {
            if(!originalKeyToCloneKey.containsKey(rootKey))
            {
                throw new IllegalArgumentException("Root key must be present in original to clone key map.");
            }

            extractParentTemplate(parentPath, Arrays.asList(rootKey), parentKey);
            ConfigurationRefactoringManager.this.clone(parentPath, originalKeyToCloneKey);
            return PathUtils.getPath(parentPath, originalKeyToCloneKey.get(rootKey));
        }

    }

    private class ExtractParentTemplateAction implements ConfigurationTemplateManager.Action<String>
    {
        private final String parentPath;
        private final String parentTemplateName;
        private final List<String> keys;

        public ExtractParentTemplateAction(String parentPath, String parentTemplateName, List<String> keys)
        {
            this.parentPath = parentPath;
            this.parentTemplateName = parentTemplateName;
            this.keys = keys;
        }

        public String execute() throws ValidationException
        {
            // First: what happens to references?  And state?  They want to stay
            // pointing at the same handles, so best to keep the current records
            // 'as-is' and pull the values up.
            if(!configurationTemplateManager.isTemplatedCollection(parentPath))
            {
                throw new IllegalArgumentException("Invalid parent path '" + parentPath + "': does not refer to a templated collection");
            }

            if(!StringUtils.stringSet(parentTemplateName))
            {
                throw new IllegalArgumentException("Parent template name is required");
            }
            
            MapType mapType = configurationTemplateManager.getType(parentPath, MapType.class);
            MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
            configurationTemplateManager.validateNameIsUnique(parentPath, parentTemplateName, mapType.getKeyProperty(), textProvider);

            if(keys.size() == 0)
            {
                throw new IllegalArgumentException("No child keys specified");
            }

            // Check all keys are siblings, and get the records while we go.
            TemplateNode templateParentNode = null;
            List<Pair<String, Record>> records = new LinkedList<Pair<String, Record>>();
            for(String key: keys)
            {
                String path = getPath(parentPath, key);
                TemplateNode node = configurationTemplateManager.getTemplateNode(path);
                if(node == null)
                {
                    throw new IllegalArgumentException("Invalid child key '" + key + "': does not refer to an element of the templated collection");
                }

                records.add(new Pair<String, Record>(path, ((TemplateRecord) configurationTemplateManager.getRecord(path)).getMoi()));

                if(node.getParent() == null)
                {
                    throw new IllegalArgumentException("Invalid child key '" + key + "': cannot extract parent from the root of a template hierarchy");
                }

                if(templateParentNode == null)
                {
                    // First child, initialise the template parent path
                    templateParentNode = node.getParent();
                }
                else if(node.getParent() != templateParentNode)
                {
                    throw new IllegalArgumentException("Invalid child keys: all child keys must refer to siblings in the template hierarchy");
                }
            }

            assert templateParentNode != null;

            // Suspend caching of instances completely, rather than trying to
            // manage the cache throughout the factoring.  Note that this
            // means the instance cache should not be touched until the
            // operation is complete!
            configurationTemplateManager.suspendInstanceCache();

            // Extract and save the new parent.
            MapType parentType = configurationTemplateManager.getType(parentPath, MapType.class);
            MutableRecord common = extractCommon(records, parentPath, parentType.getTargetType());
            common.put(mapType.getKeyProperty(), parentTemplateName);
            configurationTemplateManager.markAsTemplate(common);
            configurationTemplateManager.setParentTemplate(common, recordManager.select(templateParentNode.getPath()).getHandle());
            String newParentTemplatePath = configurationTemplateManager.insertRecord(parentPath, common);
            TemplateRecord newParentTemplateRecord = (TemplateRecord) configurationTemplateManager.getRecord(newParentTemplatePath);
            long newParentTemplateHandle = newParentTemplateRecord.getHandle();

            // Fix internal references which were pulled up (note we need to
            // refresh the record after saving).
            MutableRecord newParentTemplateCopy = newParentTemplateRecord.getMoi().copy(true);
            newParentTemplateCopy.forEach(new PullUpReferencesFunction(mapType.getTargetType(), newParentTemplateCopy, newParentTemplatePath, PathUtils.getPath(parentPath, keys.get(0))));
            newParentTemplateRecord = (TemplateRecord) configurationTemplateManager.getRecord(newParentTemplatePath);
            
            // Reparent all the children.  Note we need to scrub all the
            // values we just pulled up and then do a deep update.  No events
            // need to be sent as no concrete instance is changed.
            for (String key: keys)
            {
                String path = getPath(parentPath, key);
                MutableRecord copy = ((TemplateRecord) configurationTemplateManager.getRecord(path)).getMoi().copy(false);

                // Update the parent reference.
                configurationTemplateManager.setParentTemplate(copy, newParentTemplateHandle);
                recordManager.update(path, copy);

                // Fix references, scrub and apply updates.
                copy = ((TemplateRecord) configurationTemplateManager.getRecord(path)).getMoi().copy(true);
                copy.forEach(new CanonicaliseReferencesFunction(mapType.getTargetType(), copy, path));
                configurationTemplateManager.scrubInheritedValues(newParentTemplateRecord, copy, true);
                copy.forEach(new DeepUpdateFunction(path));
            }

            configurationTemplateManager.resumeInstanceCache();
            return newParentTemplatePath;
        }

        public MutableRecord extractCommon(List<Pair<String, Record>> pathRecordPairs, String parentPath, ComplexType type)
        {
            Map<String, String> commonMeta = new HashMap<String, String>();
            Map<String, Object> commonSimple = new HashMap<String, Object>();
            Set<String> commonNestedKeySet = new HashSet<String>();
            boolean first = true;
            String firstTemplateOwnerPath = null;
            String symbolicName = null;
            for(Pair<String, Record> pair: pathRecordPairs)
            {
                Record r = pair.second;
                if(first)
                {
                    first = false;
                    symbolicName = r.getSymbolicName();
                    commonMeta.putAll(getMeta(r));

                    // Keys that aren't inherited can't be extracted!
                    for(String key: TemplateRecord.NO_INHERIT_META_KEYS)
                    {
                        commonMeta.remove(key);
                    }

                    firstTemplateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(pair.first);
                    commonSimple.putAll(getSimple(r));
                    commonNestedKeySet.addAll(r.nestedKeySet());
                }
                else
                {
                    CollectionUtils.retainAll(commonMeta, getMeta(r));
                    commonNestedKeySet.retainAll(r.nestedKeySet());

                    if (type instanceof CollectionType)
                    {
                        CollectionUtils.retainAll(commonSimple, getSimple(r));
                    }
                    else
                    {
                        mergeSimple(firstTemplateOwnerPath, configurationTemplateManager.getTemplateOwnerPath(pair.first), commonSimple, r, (CompositeType) type);
                    }
                }
            }

            MutableRecord result = new MutableRecordImpl();
            result.setSymbolicName(symbolicName);

            for(Map.Entry<String, String> entry: commonMeta.entrySet())
            {
                result.putMeta(entry.getKey(), entry.getValue());
            }

            for(Map.Entry<String, Object> entry: commonSimple.entrySet())
            {
                result.put(entry.getKey(), entry.getValue());
            }

            if (type.hasSignificantKeys())
            {
                outer:
                for (String key : commonNestedKeySet)
                {
                    List<Pair<String, Record>> allNested = new LinkedList<Pair<String, Record>>();
                    ComplexType nestedType = null;
                    for (Pair<String, Record> pair : pathRecordPairs)
                    {
                        Record nested = (Record) pair.second.get(key);
                        if (nestedType == null)
                        {
                            nestedType = (ComplexType) type.getActualPropertyType(key, nested);
                        }
                        else if (nestedType != type.getActualPropertyType(key, nested))
                        {
                            // Type mismatch, can't extract common for this key.
                            break outer;
                        }

                        allNested.add(new Pair<String, Record>(PathUtils.getPath(pair.first, key), nested));
                    }

                    MutableRecord common = extractCommon(allNested, PathUtils.getPath(parentPath, key), nestedType);
                    if (common != null)
                    {
                        result.put(key, common);
                    }
                }
            }
            else
            {
                extractCommonValues((CollectionType) type, pathRecordPairs, result);
            }

            return result;
        }

        private void mergeSimple(String firstTemplateOwnerPath, String templateOwnerPath, Map<String, Object> commonSimple, Record record, CompositeType compositeType)
        {
            Set<String> commonKeys = commonSimple.keySet();
            commonKeys.retainAll(record.simpleKeySet());

            Iterator<String> it = commonKeys.iterator();
            while (it.hasNext())
            {
                String key = it.next();
                TypeProperty property = compositeType.getProperty(key);
                if (property == null)
                {
                    throw new IllegalStateException("Record of type '" + compositeType.getClazz().getName() + "' has unrecognised property '" + key + "'");
                }

                Type propertyType = property.getType();
                boolean equal;
                if (propertyType instanceof ListType)
                {
                    // List of references.
                    String[] commonValue = (String[]) commonSimple.get(key);
                    String[] recordValue = (String[]) record.get(key);

                    if (commonValue.length == recordValue.length)
                    {
                        equal = true;
                        for (int i = 0; i < commonValue.length; i++)
                        {
                            if (!equivalentHandles(firstTemplateOwnerPath, commonValue[i], templateOwnerPath, recordValue[i]))
                            {
                                equal = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        equal = false;
                    }
                }
                else if (propertyType instanceof ReferenceType)
                {
                    equal = equivalentHandles(firstTemplateOwnerPath, commonSimple.get(key), templateOwnerPath, record.get(key));
                }
                else
                {
                    equal = RecordUtils.valuesEqual(record.get(key), commonSimple.get(key));
                }

                if (!equal)
                {
                    it.remove();
                }
            }
        }

        private boolean equivalentHandles(String templateOwnerPath1, Object h1, String templateOwnerPath2, Object h2)
        {
            if (h1 == null)
            {
                return h2 == null;
            }

            if (!(h1 instanceof String) || !(h2 instanceof String))
            {
                return false;
            }

            String s1 = (String) h1;
            String s2 = (String) h2;

            if (s1.equals(s2))
            {
                return true;
            }

            long l1 = Long.parseLong(s1);
            long l2 = Long.parseLong(s2);

            String path1 = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath1, l1);
            String path2 = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath2, l2);

            if (path1.startsWith(templateOwnerPath1) && path2.startsWith(templateOwnerPath2))
            {
                // Both are references within the owner - they are equal if
                // they point to the same relative path within the owner, and
                // the referenced paths are of the same type.
                String type1 = recordManager.select(path1).getSymbolicName();
                String type2 = recordManager.select(path2).getSymbolicName();
                if (!type1.equals(type2))
                {
                    return false;
                }

                path1 = path1.substring(templateOwnerPath1.length());
                path2 = path2.substring(templateOwnerPath2.length());
            }

            return path1.equals(path2);
        }

        private void extractCommonValues(CollectionType type, List<Pair<String, Record>> pathRecordPairs, MutableRecord result)
        {
            CompositeType childType = (CompositeType) type.getCollectionType();
            int i = 0;
            List<ValueInfo> commonValues = new LinkedList<ValueInfo>();
            for(Pair<String, Record> pair: pathRecordPairs)
            {
                Record r = pair.second;
                if(i == 0)
                {
                    // First time round, establish a base for the common
                    // element set.
                    for(String key: r.nestedKeySet())
                    {
                        ValueInfo info = new ValueInfo((Record) r.get(key), key);
                        commonValues.add(info);
                    }
                }
                else
                {
                    for(String key: r.nestedKeySet())
                    {
                        for(ValueInfo common: commonValues)
                        {
                            if(common.getCount() < i)
                            {
                                // Not yet matched, see if this value matches.
                                Record value = (Record) r.get(key);
                                if(childType.deepValueEquals(common.record, value))
                                {
                                    common.addKey(pair.first, key);
                                }
                            }
                        }

                        Iterator<ValueInfo> it = commonValues.iterator();
                        while(it.hasNext())
                        {
                            if(it.next().getCount() < i)
                            {
                                // Not matched this time round, remove.
                                it.remove();
                            }
                        }
                    }
                }

                i++;
            }

            for(ValueInfo common: commonValues)
            {
                result.put(common.firstKey, common.record);

                // All the other keys are essentially being renamed to the
                // first one, meaning we need to update the parent and its
                // template descendents.  We also need to skeletonise the
                // list item manually as normal scrubbing cannot handle
                // complex values.
                ComplexType itemType = typeRegistry.getType(common.record.getSymbolicName());
                Record skeleton = configurationTemplateManager.createSkeletonRecord(itemType, common.record);
                for (Pair<String, String> pathKey: common.pathKeyPairs)
                {
                    if (!common.firstKey.equals(pathKey.second))
                    {
                        configurationTemplateManager.updateCollectionReferences(type, pathKey.first, pathKey.second, common.firstKey);
                    }

                    // Skeletonise the extracted list item.
                    String oldPath = PathUtils.getPath(pathKey.first, pathKey.second);
                    String newPath = PathUtils.getPath(pathKey.first, common.firstKey);
                    recordManager.move(oldPath, newPath);
                    skeleton.forEach(new DeepSkeletoniseFunction(newPath));
                }
            }
        }

        public Map<String, String> getMeta(Record r)
        {
            Set<String> keys = r.metaKeySet();
            Map<String, String> result = new HashMap<String, String>(keys.size());
            for(String key: keys)
            {
                result.put(key, r.getMeta(key));
            }

            return result;
        }

        public Map<String, Object> getSimple(Record r)
        {
            Set<String> keys = r.simpleKeySet();
            Map<String, Object> result = new HashMap<String, Object>(keys.size());
            for(String key: keys)
            {
                result.put(key, r.get(key));
            }

            return result;
        }
    }

    private static class ValueInfo
    {
        Record record;
        String firstKey;
        List<Pair<String, String>> pathKeyPairs = new LinkedList<Pair<String, String>>();

        public ValueInfo(Record record, String firstKey)
        {
            this.firstKey = firstKey;
            this.record = record;
        }

        public void addKey(String path, String key)
        {
            pathKeyPairs.add(new Pair<String, String>(path, key));
        }

        public int getCount()
        {
            return pathKeyPairs.size();
        }
    }

    /**
     * Base class for record walking functions that need to be aware of the
     * types of the records.
     */
    private static abstract class TypeAwareFunction implements GraphFunction<Record>
    {
        protected Stack<ComplexType> typeStack = new Stack<ComplexType>();
        protected Stack<MutableRecord> recordStack = new Stack<MutableRecord>();
        protected String path;

        public TypeAwareFunction(ComplexType type, MutableRecord record, String path)
        {
            typeStack.push(type);
            recordStack.push(record);
            this.path = path;
        }

        public void push(String edge)
        {
            path = PathUtils.getPath(path, edge);
            MutableRecord record = (MutableRecord) recordStack.peek().get(edge);
            typeStack.push((ComplexType) typeStack.peek().getActualPropertyType(edge, record));
            recordStack.push(record);
        }

        public void process(com.zutubi.tove.type.record.Record record)
        {
            process(record, typeStack.peek());
        }

        public void pop()
        {
            path = PathUtils.getParentPath(path);
            recordStack.pop();
            typeStack.pop();
        }

        protected abstract void process(com.zutubi.tove.type.record.Record record, Type type);
    }

    /**
     * Base for record walking functions that process references in those
     * records in some way.
     */
    private abstract class ReferenceWalkingFunction extends TypeAwareFunction
    {
        protected String templateOwnerPath;

        public ReferenceWalkingFunction(ComplexType type, MutableRecord record, String path)
        {
            super(type, record, path);
            templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
        }

        protected  void process(Record record, Type type)
        {
            if (type instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) type;
                for (TypeProperty property: compositeType.getProperties(ReferenceType.class))
                {
                    String value = (String) record.get(property.getName());
                    if (value != null)
                    {
                        handleReference(record, property, value);
                    }
                }

                for (TypeProperty property: compositeType.getProperties(ListType.class))
                {
                    Type targetType = property.getType().getTargetType();
                    if (targetType instanceof ReferenceType)
                    {
                        String[] value = (String[]) record.get(property.getName());
                        if (value != null)
                        {
                            handleReferenceList(record, property, value);
                        }
                    }
                }
            }
        }

        protected abstract void handleReferenceList(Record record, TypeProperty property, String[] value);
        protected abstract void handleReference(Record record, TypeProperty property, String value);
    }

    /**
     * A record walking function that pulls up references from an original
     * owner path into the new parent owner path.  This relies on the fact that
     * all pulled-up references use the handle from the first sibling in the
     * smart clone operation -- the from owner is this sibling.
     */
    private class PullUpReferencesFunction extends ReferenceWalkingFunction
    {
        private String fromOwnerPath;

        public PullUpReferencesFunction(ComplexType type, MutableRecord record, String path, String fromOwnerPath)
        {
            super(type, record, path);
            this.fromOwnerPath = fromOwnerPath;
        }

        @Override
        protected void process(Record record, Type type)
        {
            super.process(record, type);
            if (!record.shallowEquals(recordManager.select(path)))
            {
                recordManager.update(path, record);
                configurationTemplateManager.refreshCaches();
            }
        }

        protected void handleReference(Record record, TypeProperty property, String value)
        {
            ((MutableRecord) record).put(property.getName(), pullUp(value));
        }

        protected void handleReferenceList(Record record, TypeProperty property, String[] value)
        {
            for (int i = 0; i < value.length; i++)
            {
                value[i] = pullUp(value[i]);
            }
        }

        private String pullUp(String value)
        {
            long originalHandle = Long.parseLong(value);
            if (originalHandle == 0)
            {
                return "0";
            }
            else
            {
                String path = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, originalHandle);
                if (path.startsWith(fromOwnerPath))
                {
                    path = templateOwnerPath + path.substring(fromOwnerPath.length());
                }

                long upHandle = configurationReferenceManager.getReferenceHandleForPath(path);
                return Long.toString(upHandle);
            }
        }
    }

    /**
     * Record walking function that ensures all references are pointing to the
     * first point of definition in the hierarchy.  After an extraction, any
     * internal references may be pointing to items that have now been pulled
     * up into the parent -- these reference handles need fixing.
     */
    private class CanonicaliseReferencesFunction extends ReferenceWalkingFunction
    {
        public CanonicaliseReferencesFunction(ComplexType type, MutableRecord record, String path)
        {
            super(type, record, path);
        }

        protected void handleReference(Record record, TypeProperty property, String value)
        {
            ((MutableRecord) record).put(property.getName(), canonicaliseReference(value));
        }

        protected void handleReferenceList(Record record, TypeProperty property, String[] value)
        {
            for (int i = 0; i < value.length; i++)
            {
                value[i] = canonicaliseReference(value[i]);
            }
        }

        private String canonicaliseReference(String value)
        {
            long originalHandle = Long.parseLong(value);
            if (originalHandle == 0)
            {
                return "0";
            }
            else
            {
                String path = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, originalHandle);
                long canonicalisedHandle = configurationReferenceManager.getReferenceHandleForPath(path);
                return Long.toString(canonicalisedHandle);
            }
        }
    }

    /**
     * Record walking function that checks if any unsaved changes have been
     * made to a record, and if so saves them.
     */
    private class DeepUpdateFunction implements GraphFunction<Record>
    {
        private String path;

        public DeepUpdateFunction(String path)
        {
            this.path = path;
        }

        public void push(String edge)
        {
            path = PathUtils.getPath(path, edge);
        }

        public void process(Record record)
        {
            if (!record.shallowEquals(recordManager.select(path)))
            {
                recordManager.update(path, record);
                configurationTemplateManager.refreshCaches();
            }
        }

        public void pop()
        {
            path = PathUtils.getParentPath(path);
        }
    }

    /**
     * Record walking function that applies the skeletal record tree to the
     * existing persistent records, keeping handles intact.
     */
    private class DeepSkeletoniseFunction implements GraphFunction<Record>
    {
        private String path;

        public DeepSkeletoniseFunction(String path)
        {
            this.path = path;
        }

        public void push(String edge)
        {
            path = PathUtils.getPath(path, edge);
        }

        public void process(Record record)
        {
            Record existing = recordManager.select(path);
            if (!record.shallowEquals(existing))
            {
                MutableRecord r = record.copy(false);
                r.setHandle(existing.getHandle());
                recordManager.update(path, r);
                configurationTemplateManager.refreshCaches();
            }
        }

        public void pop()
        {
            path = PathUtils.getParentPath(path);
        }
    }
}
