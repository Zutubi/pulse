package com.zutubi.prototype.config;

import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.*;
import static com.zutubi.prototype.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
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

    /**
     * Tests if the given path is cloneable.  This does <b>not</b> take into
     * account security: i.e. it does not check that the user can insert into
     * the parent path.
     *
     * @param path the path to test, which much exist
     * @return true iff the path can be cloned; it must be a map item and not
     *         the root of a template hierarch
     */
    public boolean canClone(String path)
    {
        String parentPath = PathUtils.getParentPath(path);
        if(parentPath != null && configurationTemplateManager.pathExists(path))
        {
            Type parentType = configurationTemplateManager.getType(parentPath);
            if(parentType instanceof MapType)
            {
                return !configurationTemplateManager.isTemplatedCollection(parentPath) || configurationTemplateManager.getTemplateParentRecord(path) != null;
            }
        }

        return false;
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
    public String clone(String path, String cloneKey)
    {
        // Validate new name, get a clone of the instance, set new key, insert?
        String parentPath = PathUtils.getParentPath(path);
        if (parentPath == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
        }

        clone(parentPath, asMap(asPair(PathUtils.getBaseName(path), cloneKey)));
        return getPath(parentPath, cloneKey);
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

            String path = getPath(parentPath, originalKey);
            Record record = configurationTemplateManager.getRecord(path);
            if (record == null)
            {
                throw new IllegalArgumentException("Invalid path '" + path + "': path does not exist");
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
                            // Does the referenced path fall into the clone set?
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
                rewriteRecordReferences(getPath(path, key), (Record) record.get(key), parentPath, oldKeyToNewKey);
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
}
