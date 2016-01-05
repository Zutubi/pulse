package com.zutubi.tove.config;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.zutubi.i18n.Messages;
import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.config.health.ConfigurationHealthReport;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.MessagesTextProvider;

import java.util.*;

import static com.zutubi.tove.type.record.PathUtils.*;

/**
 * Provides high-level refactoring actions for configuration.
 */
public class ConfigurationRefactoringManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRefactoringManager.class);
    private static final Messages I18N = Messages.getInstance(ConfigurationRefactoringManager.class);

    public static final String ACTION_CLONE = "clone";
    public static final String ACTION_PULL_UP = "pullUp";
    public static final String ACTION_PUSH_DOWN = "pushDown";
    public static final String ACTION_INTRODUCE_PARENT = "introduceParent";

    private TypeRegistry typeRegistry;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationReferenceManager configurationReferenceManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationHealthChecker configurationHealthChecker;
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
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
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
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<String>()
        {
            public String process()
            {
                String parentPath = PathUtils.getParentPath(path);
                if (parentPath == null)
                {
                    throw new IllegalArgumentException("Invalid path '" + path + "': no parent");
                }

                ConfigurationRefactoringManager.this.clone(parentPath, ImmutableMap.of(PathUtils.getBaseName(path), cloneKey));
                return getPath(parentPath, cloneKey);
            }
        });
    }

    /**
     * Tests if the given path can have a template parent introduced above it.
     * Parents can be introduced above items of templated collections excepting
     * the root of the template tree.
     *
     * @see #introduceParentTemplate(String, java.util.List, String, boolean) 
     *
     * @param path the path to test
     * @return true iff a parent template may be introduced above the given
     *         path
     */
    public boolean canIntroduceParentTemplate(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                return configurationTemplateManager.pathExists(path) && configurationTemplateManager.isTemplatedCollection(PathUtils.getParentPath(path)) && configurationTemplateManager.getTemplateParentRecord(path) != null;
            }
        });
    }

    /**
     * Introduces a new parent template over a set of siblings.  The new
     * template may be empty, or include all common configuration from the
     * siblings.  After the operation is complete, the final values of all
     * fields (in the deep sense) of each sibling is unchanged.  If pullUp is
     * true, all values common to every sibling will be inherited from the
     * new parent.
     *
     * @param parentPath         path of the templated collection that the
     *                           siblings are items of
     * @param keys               keys of the sibling items to introduce the
     *                           parent above (all keys must refer to items
     *                           with the same template parent)
     * @param parentTemplateKey  key for the new template parent
     * @param pullUp             if true, pull up all common values from
     *                           the children into the new parent; otherwise
     *                           create a new empty parent
     * @return the path of the new template parent
     * @throws IllegalArgumentException if the the parent path is not a
     *         templated collection; if no keys are specified; if the keys do
     *         not share a common, non-null template parent; if the parent
     *         template name is invalid or in use
     */
    public String introduceParentTemplate(final String parentPath, final List<String> keys, final String parentTemplateKey, boolean pullUp)
    {
        return configurationTemplateManager.executeInsideTransaction(new IntroduceParentTemplateAction(parentPath, parentTemplateKey, keys, pullUp));
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
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                return canIntroduceParentTemplate(path) && canClone(path);
            }
        });
    }

    /**
     * Clones a templated map item by extracting its details into a parent
     * template and adding a skeletal sibling (the clone).  The item's
     * descendants may also optionally be cloned, although those clones
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

    /**
     * Returns a list of all ancestors that the given path may be pulled up to.
     * If the path cannot be pulled up at all, the list will be empty.  The
     * requirements for pulling up are documented on {@link #canPullUp(String, String)}.
     *
     * @param path the path that is a candidate to pull up
     * @return the keys of all ancestors that the path may be pulled up to
     */
    public List<String> getPullUpAncestors(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
                if (templateOwnerPath == null)
                {
                    return Collections.emptyList();
                }

                List<String> result = new LinkedList<String>();
                TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
                node = node.getParent();
                while (node != null)
                {
                    if (canPullUp(path, node.getId()))
                    {
                        result.add(node.getId());
                    }

                    node = node.getParent();
                }

                return result;
            }
        });
    }

    /**
     * Tests if a given path can be pulled up in the template hierarchy to any
     * of its ancestors.  The requirements for pulling up are documented on
     * {@link #canPullUp(String, String)}.
     * <p/>
     * <b>Note</b> that this method does not take security into account: i.e.
     * it does not check that the user has permission to write into the
     * ancestor.
     *
     * @param path the path to test
     * @return true iff the path may be pulled up at least one of its ancestors
     */
    public boolean canPullUp(final String path)
    {
        return !getPullUpAncestors(path).isEmpty();
    }

    /**
     * Tests if a given path can be pulled up in the template hierarchy to the
     * given ancestor.  To be pulled up a path must:
     *
     * <ul>
     *   <li>Refer to an existing item of composite type</li>
     *   <li>Be in a templated scope</li>
     *   <li>Not be marked permanent</li>
     *   <li>Not be defined in any ancestor</li>
     *   <li>Not be defined in any other descendant of the specified ancestor</li>
     *   <li>Not contain any references to items not visible from the specified ancestor</li>
     * </ul>
     *
     * The given ancestor must be a strict ancestor of the templated collection
     * item that owns the specified path.
     * <p/>
     * <b>Note</b> that this method does not take security into account: i.e.
     * it does not check that the user has permission to write into the
     * ancestor.

     * @param path        the path to test
     * @param ancestorKey key of the templated collection item to pull the path
     *                    up to (must be an ancestor of the paths template owner)
     * @return true iff the path may be pulled up
     */
    public boolean canPullUp(final String path, final String ancestorKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new CanPullUpAction(path, ancestorKey));
    }

    /**
     * Pulls up an item from its current location in the template hierarchy to
     * the given ancestor.  This is not possible for all paths, see
     * {@link #canPullUp(String, String)} for details.
     *
     * @param path        path of the item to pull up
     * @param ancestorKey key of the templated collection item to pull the path
     *                    up to (must be an ancestor of the paths template owner)
     * @return the path of the pulled up item
     * @throws IllegalArgumentException if the given path or ancestor are invalid,
     *         or the path cannot be pulled up
     */
    public String pullUp(final String path, final String ancestorKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new PullUpAction(path, ancestorKey));
    }

    /**
     * Returns a list of all children that the given path may be pushed down
     * to.  If the path cannot be pushed down at all, the list will be empty.
     * The requirements for pushing down are documented on {@link #canPushDown(String, String)}.
     *
     * @param path the path that is a candidate to push down
     * @return the keys of all children that the path may be pushed down to
     */
    public List<String> getPushDownChildren(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
                if (templateOwnerPath == null)
                {
                    return Collections.emptyList();
                }

                List<String> result = new LinkedList<String>();
                TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
                for (TemplateNode childNode: node.getChildren())
                {
                    if (canPushDown(path, childNode.getId()))
                    {
                        result.add(childNode.getId());
                    }
                }

                return result;
            }
        });
    }

    /**
     * Tests if a given path can be pushed down in the template hierarchy to
     * any of its children.  The requirements for pushing down are documented
     * on {@link #canPushDown(String, String)}.
     * <p/>
     * <b>Note</b> that this method does not take security into account: i.e.
     * it does not check that the user has permission to write into the
     * children.
     *
     * @param path the path to test
     * @return true iff the path may be pushed down to at least one of its
     *         children
     */
    public boolean canPushDown(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<Boolean>()
        {
            public Boolean process()
            {
                String templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
                if (templateOwnerPath == null)
                {
                    return false;
                }

                TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
                for (TemplateNode childNode: node.getChildren())
                {
                    if (canPushDown(path, childNode.getId()))
                    {
                        return true;
                    }
                }

                return false;
            }
        });
    }
    
    /**
     * Tests if a given path can be pushed down in the template hierarchy to
     * the given child.  To be pushed down a path must:
     *
     * <ul>
     *   <li>Refer to an existing item of composite type</li>
     *   <li>Be in a templated scope</li>
     *   <li>Not be marked permanent</li>
     *   <li>Not be inherited</li>
     *   <li>Not be hidden in the child</li>
     *   <li>Not contain any references to items not visible from the specified child</li>
     * </ul>
     *
     * The given child must be a direct child of the templated collection
     * item that owns the specified path.
     * <p/>
     * <b>Note</b> that this method does not take security into account: i.e.
     * it does not check that the user has permission to write into the
     * child.

     * @param path     the path to test
     * @param childKey key of the templated collection item to push the path
     *                 down to (must be a child of the path's template owner)
     * @return true iff the path may be pulled up
     */
    public boolean canPushDown(String path, String childKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new CanPushDownAction(path, childKey));
    }

    /**
     * Pushes down an item from its current location in the template hiearchy
     * to the given children.  Each specified child must satisfy
     * {@link #canPushDown(String, String)}.  Children that hide the item
     * cannot be included, and will not have the item pushed down to them.  At
     * least one other child must be specified (otherwise this would be
     * equivalent to deleting the item -- and we would prefer the user to
     * explicitly specify that this is what they intend to do).  Any children
     * that do not hide the item and are not specified will have the item
     * deleted.
     *
     * @param path      the path to push down
     * @param childKeys keys of the children of the item's current template
     *                  owner to push the item down to
     * @return the set of child paths to which the item was pushed down
     */
    public Set<String> pushDown(String path, Set<String> childKeys)
    {
        return configurationTemplateManager.executeInsideTransaction(new PushDownAction(path, childKeys));
    }

    /**
     * Returns the keys of all template instances that a given path can be
     * moved under using {@link #move(String, String)}.  If the given path
     * cannot be moved, the list returned is empty.
     *
     * @param path the path to get move templates for
     * @return a list of keys for all templates that the path may be moved
     *         under
     * @see #move(String, String)
     */
    public List<String> getMoveTemplates(final String path)
    {
        return configurationTemplateManager.executeInsideTransaction(new NullaryFunction<List<String>>()
        {
            public List<String> process()
            {
                try
                {
                    final List<String> result = new LinkedList<String>();
                    Pair<String, String> scopeItemPair = ensurePathRefersToNonRootTemplatedCollectionItem(path, "Path");
                    TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scopeItemPair.first);
                    final TemplateNode moveNode = hierarchy.getNodeById(scopeItemPair.second);
                    hierarchy.getRoot().forEachDescendant(new Function<TemplateNode, Boolean>()
                    {
                        public Boolean apply(TemplateNode node)
                        {
                            if (node == moveNode)
                            {
                                return false;
                            }
                            else
                            {
                                if (!node.isConcrete() && configurationSecurityManager.hasPermission(node.getPath(), AccessManager.ACTION_VIEW))
                                {
                                    result.add(node.getId());
                                }

                                return true;
                            }
                        }
                    }, false, null);

                    return result;
                }
                catch (IllegalArgumentException e)
                {
                    return Collections.emptyList();
                }
            }
        });
    }

    /**
     * Previews a move of an item in a templated collection to a new parent,
     * without making any actual changes.  This allows the caller to examine
     * the result of a move before going ahead.  An example use-case is warning
     * the user of incompatible paths that would need to be deleted to make the
     * move.
     *
     * @param path                  path of the item to move, must refer to a
     *                              non-root templated collection item
     * @param newTemplateParentKey  key of the new template parent to move to,
     *                              in the same collection as path, must refer
     *                              to a template not under the path in the
     *                              hierarchy
     * @return a result instance giving details of the move
     *
     * @see #move(String, String)
     */
    public MoveResult previewMove(String path, String newTemplateParentKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new PreviewMoveAction(path, newTemplateParentKey));
    }

    /**
     * Moves an item of a templated collection to a new location in the
     * template hierarchy.  If the moved item has descendants, they are moved
     * too.
     * <p/>
     * When moving an item, some paths may need to be deleted.  Any path in the
     * record to be moved that is type-incompatible with the corresponding path
     * in the new template parent will be deleted.
     * <p/>
     * Note that moving can also create new concrete instances - if they are
     * defined in the new template parent and did not previous exist in the
     * concrete leaves of the moved subtree.
     * 
     * @param path                  path of the item to move, must refer to a
     *                              non-root templated collection item
     * @param newTemplateParentKey  key of the new template parent to move to,
     *                              in the same collection as path, must refer
     *                              to a template not under the path in the
     *                              hierarchy
     * @return a result instance giving details of the move
     * 
     * @see #previewMove(String, String)
     * @see #getMoveTemplates(String) 
     */
    public MoveResult move(String path, String newTemplateParentKey)
    {
        return configurationTemplateManager.executeInsideTransaction(new MoveAction(path, newTemplateParentKey));
    }

    /**
     * Contains information about what happened during a move of a templated
     * collection item.
     */
    public static class MoveResult
    {
        private List<String> deletedPaths = new LinkedList<String>();

        /**
         * Indicates paths that were deleted by the move, due to
         * incompatibilities with the new template parent.
         * 
         * @return the paths that were deleted by the move
         */
        public List<String> getDeletedPaths()
        {
            return Collections.unmodifiableList(deletedPaths);
        }
        
        private void addDeletedPath(String path)
        {
            deletedPaths.add(path);
        }
    }

    /**
     * Ensures the given string is set.
     * 
     * @param s    the string to test
     * @param name capitalised, human-readable name for the string (e.g.
     *             "Clone path")
     */
    private void ensureSpecified(String s, String name)
    {
        if (!StringUtils.stringSet(s))
        {
            throw new IllegalArgumentException(I18N.format("string.required", name));
        }
    }
    
    /**
     * Ensures the given path is set and refers to an existing configuration
     * instance.
     * 
     * @param path     the path to test
     * @param pathName capitalised, human-readable name for the path (e.g.
     *                 "Clone path")
     */
    private void ensurePathExists(String path, String pathName)
    {
        ensureSpecified(path, pathName);
        if (!configurationTemplateManager.pathExists(path))
        {
            throw new IllegalArgumentException(I18N.format("path.nonexistant", pathName, path));
        }
    }

    /**
     * Ensures the given path refers to an existing configuration instances in
     * a templated scope.
     * 
     * @param path     the path to test
     * @param pathName capitalised, human-readable name for the path (e.g.
     *                 "Clone path")
     */
    private void ensurePathExistsInTemplatedScope(String path, String pathName)
    {
        ensurePathExists(path, pathName);
        if (!configurationTemplateManager.isTemplatedPath(path))
        {
            throw new IllegalArgumentException(I18N.format("path.not.templated", pathName, path));
        }
    }

    /**
     * Ensures the given path refers to an existing templated collection item.
     * 
     * @param path     the path to test
     * @param pathName capitalised, human-readable name for the path (e.g.
     *                 "Clone path")
     */
    private Pair<String, String> ensurePathRefersToTemplatedCollectionItem(String path, String pathName)
    {
        ensurePathExistsInTemplatedScope(path, pathName);
        String[] elements = PathUtils.getPathElements(path);
        if (elements.length != 2)
        {
            throw new IllegalArgumentException(I18N.format("path.not.templated.collection.item", pathName, path));
        }
        
        return new Pair<String, String>(elements[0], elements[1]);
    }

    /**
     * Ensures the given path refers to an existing templated collection item,
     * but not the root of the templated scope.
     * 
     * @param path     the path to test
     * @param pathName capitalised, human-readable name for the path (e.g.
     *                 "Clone path")
     */
    private Pair<String, String> ensurePathRefersToNonRootTemplatedCollectionItem(String path, String pathName)
    {
        Pair<String, String> result = ensurePathRefersToTemplatedCollectionItem(path, pathName);
        if (configurationTemplateManager.getTemplateParentHandle(path, configurationTemplateManager.getRecord(path)) == 0)
        {
            throw new IllegalArgumentException(I18N.format("path.is.root", pathName, path));
        }
        
        return result;
    }

    /**
     * Ensures the given path refers to an existing, non-concrete templated
     * collection item.
     * 
     * @param path     the path to test
     * @param pathName capitalised, human-readable name for the path (e.g.
     *                 "Clone path")
     */
    private Pair<String, String> ensurePathRefersToNonConcreteTemplatedCollectionItem(String path, String pathName)
    {
        Pair<String, String> result = ensurePathRefersToTemplatedCollectionItem(path, pathName);
        if (configurationTemplateManager.isConcrete(path))
        {
            throw new IllegalArgumentException(I18N.format("path.is.concrete", pathName, path));
        }
        
        return result;
    }

    public void setConfigurationHealthChecker(ConfigurationHealthChecker configurationHealthChecker)
    {
        this.configurationHealthChecker = configurationHealthChecker;
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

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    /**
     * Action used to implement {@link ConfigurationRefactoringManager#clone(String, java.util.Map)}.
     */
    private class CloneAction implements NullaryFunction<Object>
    {
        private final String parentPath;
        private final Map<String, String> originalKeyToCloneKey;

        public CloneAction(String parentPath, Map<String, String> originalKeyToCloneKey)
        {
            this.parentPath = parentPath;
            this.originalKeyToCloneKey = originalKeyToCloneKey;
        }

        public Object process()
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
            String scope = null;
            boolean nestedWithinTemplatedOwner = false;
            TemplateNode templateNode = null;
            if (templatedCollection)
            {
                // Sort the keys in order of template depth so we can be
                // sure that a parent is cloned before its children.
                originalKeys = sortKeys(parentPath, originalKeyToCloneKey);
            }
            else
            {
                originalKeys = originalKeyToCloneKey.keySet();

                String[] pathElements = getPathElements(parentPath);
                scope = getPath(0, 1, pathElements);
                if (configurationTemplateManager.isTemplatedCollection(scope))
                {
                    // We are cloning an item nested within a templated owner,
                    // skeletons may be required if we have descendants.
                    nestedWithinTemplatedOwner = true;
                    templateNode = configurationTemplateManager.getTemplateNode(getPath(0, 2, pathElements));
                }
            }

            // Copy each record, updating parent references if necessary as
            // we go (reparenting is not safe due to scrubbing).
            Set<String> seenNames = new HashSet<String>();
            List<String> newConcretePaths = new LinkedList<String>();
            for (String originalKey: originalKeys)
            {
                String cloneKey = originalKeyToCloneKey.get(originalKey);
                Record record = checkKeys(parentPath, originalKey, cloneKey, templatedCollection, seenNames);
                try
                {
                    configurationTemplateManager.validateNameIsUnique(parentPath, cloneKey, keyPropertyName, textProvider);
                }
                catch (ValidationException e)
                {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }

                // CIB-2307 - we need the specific instances target type, not the generic type
                // represented by the mapType.getTargetType()
                CompositeType cloneType = (CompositeType) mapType.getActualPropertyType(originalKey, record);
                MutableRecord clone = copySimple(record, cloneType);
                clone.put(keyPropertyName, cloneKey);
                if (templatedCollection)
                {
                    checkParent(parentPath, originalKey, clone, originalKeyToCloneKey);
                }

                String clonePath = getPath(parentPath, cloneKey);
                recordManager.insert(clonePath, clone);
                cloneChildren(clonePath, record, cloneType);

                if (nestedWithinTemplatedOwner)
                {
                    String remainderPath = getPath(2, getPathElements(clonePath));
                    configurationTemplateManager.addInheritedSkeletons(scope, remainderPath, cloneType, recordManager.select(clonePath), templateNode, true);
                    newConcretePaths.addAll(configurationTemplateManager.getDescendantPaths(clonePath, false, true, false));
                }
                else if (configurationTemplateManager.isConcrete(parentPath, clone))
                {
                    newConcretePaths.add(clonePath);
                }

            }

            // Now rewrite all internal references that fall inside the clone set
            rewriteReferences(parentPath, originalKeyToCloneKey);
            configurationTemplateManager.refreshCaches();
            configurationTemplateManager.raiseInsertEvents(newConcretePaths);
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
            // If we are cloning an element of a templated collection, we can
            // just copy the raw records as the cloned item will inherit
            // everything the original item does.  If we are cloning anything
            // else, the new clone will not inherit anything, so we take the
            // whole template record (and later flatten it) so all the values
            // in the clone are the same as the original (whether the original
            // inherited them or not).
            Record record = templatedCollection ? recordManager.select(path) : configurationTemplateManager.getRecord(path);
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

            return record instanceof TemplateRecord ? ((TemplateRecord) record).flatten(false) : record;
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

        private void cloneChildren(String path, Record record, ComplexType type)
        {
            // We walk over all child records recursively to ensure collection
            // items are inserted in the same order that they are in the
            // original records.  This ensures implicit collection ordering is
            // the preserved in the clone (CIB-2254, CIB-2564).
            for (String key: configurationTemplateManager.getOrderedNestedKeys(record, type))
            {
                Record child = (Record) record.get(key);
                ComplexType childType = (ComplexType) type.getActualPropertyType(key, child);
                MutableRecord copy = copySimple(child, childType);
                String insertPath = getPath(path, key);
                recordManager.insert(insertPath, copy);
                cloneChildren(insertPath, child, childType);
            }
        }

        private MutableRecord copySimple(Record record, ComplexType type)
        {
            MutableRecord copy = new MutableRecordImpl();
            for (String meta: record.metaKeySet())
            {
                copy.putMeta(meta, record.getMeta(meta));
            }

            for (String simple: record.simpleKeySet())
            {
                if (!isExternalStateProperty(type, simple))
                {
                    copy.put(simple, record.get(simple));
                }
            }

            return copy;
        }

        private boolean isExternalStateProperty(ComplexType type, String propertyName)
        {
            if (type instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) type;
                TypeProperty property = compositeType.getInternalProperty(propertyName);
                return property != null && property.getAnnotation(ExternalState.class) != null;
            }
            else
            {
                return false;
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
                // Lazily created only when we need to make an update.  This
                // approach makes this method quite long, in the name of
                // efficiency.
                MutableRecord mutableRecord = null;

                CompositeType type = typeRegistry.getType(symbolicName);
                for (TypeProperty property : type.getProperties(ReferenceType.class))
                {
                    Object data = record.get(property.getName());
                    if (data != null)
                    {
                        try
                        {
                            // Does the referenced path fall into the clone set?
                            ReferenceType referenceType = (ReferenceType) property.getType();
                            String referencedPath = referenceType.getReferencedPath(templateOwnerPath, data);
                            if (inCloneSet(referencedPath, parentPath, oldKeyToNewKey.keySet()))
                            {
                                // Then we need to update the reference to point to
                                // the new clone.
                                String newPath = convertPath(referencedPath, parentPath, oldKeyToNewKey);
                                long newHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, newPath);
                                if (mutableRecord == null)
                                {
                                    mutableRecord = record.copy(false, true);
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

                for (TypeProperty property: type.getProperties(ListType.class))
                {
                    Type targetType = property.getType().getTargetType();
                    if (targetType instanceof ReferenceType)
                    {
                        String[] value = (String[]) record.get(property.getName());
                        if (value != null)
                        {
                            ReferenceType referenceType = (ReferenceType) targetType;
                            // Created when we find something to update - if no
                            // updates are made this will be null after the loop.
                            String[] newValue = null;
                            for (int i = 0; i < value.length; i++)
                            {
                                try
                                {
                                    String referencedPath = referenceType.getReferencedPath(templateOwnerPath, value[i]);
                                    if (inCloneSet(referencedPath, parentPath, oldKeyToNewKey.keySet()))
                                    {
                                        String newPath = convertPath(referencedPath, parentPath, oldKeyToNewKey);
                                        long newHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, newPath);
                                        if (newValue == null)
                                        {
                                            newValue = new String[value.length];
                                            System.arraycopy(value, 0, newValue, 0, value.length);
                                        }

                                        newValue[i] = Long.toString(newHandle);
                                    }
                                }
                                catch (TypeException e)
                                {
                                    LOG.severe(e);
                                }
                            }

                            if (newValue != null)
                            {
                                if (mutableRecord == null)
                                {
                                    mutableRecord = record.copy(false, true);
                                }

                                mutableRecord.put(property.getName(), newValue);
                            }
                        }
                    }
                }

                if (mutableRecord != null)
                {
                    recordManager.update(path, mutableRecord);
                }
            }

            for (String key : record.nestedKeySet())
            {
                rewriteRecordReferences(getPath(path, key), (Record) record.get(key), parentPath, templateOwnerPath, oldKeyToNewKey);
            }
        }

        private boolean inCloneSet(String path, String parentPath, Set<String> oldKeys)
        {
            if (path != null && isUnder(path, parentPath))
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

    private class SmartCloneAction implements NullaryFunction<String>
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

        public String process()
        {
            if(!originalKeyToCloneKey.containsKey(rootKey))
            {
                throw new IllegalArgumentException("Root key must be present in original to clone key map.");
            }

            introduceParentTemplate(parentPath, Arrays.asList(rootKey), parentKey, true);
            ConfigurationRefactoringManager.this.clone(parentPath, originalKeyToCloneKey);
            return PathUtils.getPath(parentPath, originalKeyToCloneKey.get(rootKey));
        }

    }

    private class IntroduceParentTemplateAction implements NullaryFunction<String>
    {
        private final String parentPath;
        private final String parentTemplateName;
        private final List<String> keys;
        private final boolean pullUp;

        public IntroduceParentTemplateAction(String parentPath, String parentTemplateName, List<String> keys, boolean pullUp)
        {
            this.parentPath = parentPath;
            this.parentTemplateName = parentTemplateName;
            this.keys = keys;
            this.pullUp = pullUp;
        }

        public String process()
        {
            if (!configurationTemplateManager.isTemplatedCollection(parentPath))
            {
                throw new IllegalArgumentException("Invalid parent path '" + parentPath + "': does not refer to a templated collection");
            }

            if (!StringUtils.stringSet(parentTemplateName))
            {
                throw new IllegalArgumentException("Parent template name is required");
            }
            
            MapType mapType = configurationTemplateManager.getType(parentPath, MapType.class);
            MessagesTextProvider textProvider = new MessagesTextProvider(mapType.getTargetType().getClazz());
            try
            {
                configurationTemplateManager.validateNameIsUnique(parentPath, parentTemplateName, mapType.getKeyProperty(), textProvider);
            }
            catch (ValidationException e)
            {
                throw new IllegalArgumentException(e.getMessage(), e);
            }

            if (keys.size() == 0)
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

            try
            {
                // Extract and save the new parent.
                MapType parentType = configurationTemplateManager.getType(parentPath, MapType.class);
                MutableRecord newParent;
                
                if (pullUp)
                {
                    newParent = extractCommon(records, parentPath, parentType.getTargetType());
                }
                else
                {
                    newParent = new MutableRecordImpl();
                    newParent.setSymbolicName(records.get(0).second.getSymbolicName());
                }

                newParent.put(mapType.getKeyProperty(), parentTemplateName);
                configurationTemplateManager.markAsTemplate(newParent);
                configurationTemplateManager.setParentTemplate(newParent, recordManager.select(templateParentNode.getPath()).getHandle());
                String newParentTemplatePath = configurationTemplateManager.insertRecord(parentPath, newParent);
                TemplateRecord newParentTemplateRecord = (TemplateRecord) configurationTemplateManager.getRecord(newParentTemplatePath);
                long newParentTemplateHandle = newParentTemplateRecord.getHandle();

                if (pullUp)
                {
                    // Fix internal references which were pulled up (note we need to
                    // refresh the record after saving).
                    MutableRecord newParentTemplateCopy = newParentTemplateRecord.getMoi().copy(true, true);
                    newParentTemplateCopy.forEach(new PullUpReferencesFunction(mapType.getTargetType(), newParentTemplateCopy, newParentTemplatePath, PathUtils.getPath(parentPath, keys.get(0))));
                    newParentTemplateRecord = (TemplateRecord) configurationTemplateManager.getRecord(newParentTemplatePath);
                }

                // Reparent all the children.  No events need to be sent as no
                // concrete instance is changed.
                for (String key: keys)
                {
                    String path = getPath(parentPath, key);
                    MutableRecord copy = ((TemplateRecord) configurationTemplateManager.getRecord(path)).getMoi().copy(false, true);

                    // Update the parent reference.
                    configurationTemplateManager.setParentTemplate(copy, newParentTemplateHandle);
                    recordManager.update(path, copy);

                    if (pullUp)
                    {
                        // Fix references we just pulled up, scrub and apply
                        // updates.
                        copy = ((TemplateRecord) configurationTemplateManager.getRecord(path)).getMoi().copy(true, true);
                        copy.forEach(new CanonicaliseReferencesFunction(mapType.getTargetType(), copy, path));
                        configurationTemplateManager.scrubInheritedValues(newParentTemplateRecord, copy, true);
                        copy.forEach(new DeepUpdateFunction(path));
                    }
                }

                return newParentTemplatePath;
            }
            finally
            {
                configurationTemplateManager.resumeInstanceCache();
            }
        }

        private MutableRecord extractCommon(List<Pair<String, Record>> pathRecordPairs, String parentPath, ComplexType type)
        {
            Map<String, String> commonMeta = new HashMap<String, String>();
            Map<String, Object> commonSimple = new HashMap<String, Object>();
            Set<String> commonNestedKeySet = new LinkedHashSet<String>();
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
                    removeExternalState(type, commonSimple);
                    commonNestedKeySet.addAll(configurationTemplateManager.getOrderedNestedKeys(r, type));
                }
                else
                {
                    commonMeta = Maps.difference(commonMeta, getMeta(r)).entriesInCommon();
                    commonNestedKeySet.retainAll(r.nestedKeySet());

                    if (type instanceof CollectionType)
                    {
                        commonSimple = Maps.difference(commonSimple, getSimple(r)).entriesInCommon();
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

        private void removeExternalState(ComplexType type, Map<String, Object> values)
        {
            // CIB-2673: external state cannot be extracted.
            if (type instanceof CompositeType)
            {
                CompositeType compositeType = (CompositeType) type;
                for (TypeProperty property: compositeType.getInternalProperties())
                {
                    if (property.getAnnotation(ExternalState.class) != null)
                    {
                        values.remove(property.getName());
                    }
                }
            }
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

            if (isUnder(path1, templateOwnerPath1) && isUnder(path2, templateOwnerPath2))
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
                // template descendants.  We also need to skeletonise the
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

    private abstract class TemplateOwnerAwareReferenceUpdatingFunction extends ReferenceUpdatingFunction
    {
        protected String templateOwnerPath;

        public TemplateOwnerAwareReferenceUpdatingFunction(ComplexType type, MutableRecord record, String path)
        {
            super(type, record, path);
            templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
        }
    }

    /**
     * A record walking function that pulls up references from an original
     * owner path into the new parent owner path.  This relies on the fact that
     * all pulled-up references use the handle from the first sibling in the
     * smart clone operation -- the from owner is this sibling.
     */
    private class PullUpReferencesFunction extends TemplateOwnerAwareReferenceUpdatingFunction
    {
        private String fromOwnerPath;

        public PullUpReferencesFunction(ComplexType type, MutableRecord record, String path, String fromOwnerPath)
        {
            super(type, record, path);
            this.fromOwnerPath = fromOwnerPath;
        }

        @Override
        protected void process(String path, Record record, Type type)
        {
            super.process(path, record, type);
            if (!record.shallowEquals(recordManager.select(this.path)))
            {
                recordManager.update(this.path, record);
                configurationTemplateManager.refreshCaches();
            }
        }

        protected String updateReference(String value)
        {
            long originalHandle = Long.parseLong(value);
            if (originalHandle == 0)
            {
                return "0";
            }
            else
            {
                String path = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, originalHandle);
                if (isUnder(path, fromOwnerPath))
                {
                    path = templateOwnerPath + path.substring(fromOwnerPath.length());
                }

                long upHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, path);
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
    private class CanonicaliseReferencesFunction extends TemplateOwnerAwareReferenceUpdatingFunction
    {
        public CanonicaliseReferencesFunction(ComplexType type, MutableRecord record, String path)
        {
            super(type, record, path);
        }

        protected String updateReference(String value)
        {
            long originalHandle = Long.parseLong(value);
            if (originalHandle == 0)
            {
                return "0";
            }
            else
            {
                String path = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, originalHandle);
                long canonicalisedHandle = configurationReferenceManager.getReferenceHandleForPath(templateOwnerPath, path);
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
                MutableRecord r = record.copy(false, false);
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

    /**
     * Helper base class for "pull up" and "push down" actions.
     */
    private class PullPushActionSupport
    {
        protected String path;
        protected String templateOwnerPath;
        protected String remainderPath;

        private PullPushActionSupport(String path)
        {
            this.path = path;
            templateOwnerPath = configurationTemplateManager.getTemplateOwnerPath(path);
            remainderPath = PathUtils.getSuffix(path, 2);
        }

        public void ensureMovable()
        {
            // Path must be a composite in a templated scope.
            ensurePathExistsInTemplatedScope(path, "Path");

            if (!StringUtils.stringSet(remainderPath))
            {
                throw new IllegalArgumentException("Path refers to a top-level templated collection item");
            }

            Type type = configurationTemplateManager.getType(path);
            if (!(type instanceof CompositeType))
            {
                throw new IllegalArgumentException("Path does not refer to an item of composite type");
            }

            Record record = configurationTemplateManager.getRecord(path);
            if (record.isPermanent())
            {
                throw new IllegalArgumentException("Path is marked permanent");
            }
        }

        /**
         * Returns the set of all references within our template owner.  Each
         * is a pair (from path, to path).
         *
         * @return the set of all references within our template owner
         */
        protected Set<Pair<String, String>> getReferencedPaths()
        {
            final Set<Pair<String, String>> result = new HashSet<Pair<String, String>>();
            CompositeType type = (CompositeType) configurationTemplateManager.getType(templateOwnerPath);
            TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(templateOwnerPath);
            record.forEach(new ReferenceWalkingFunction(type, record, templateOwnerPath)
            {
                @Override
                protected void handleReferenceList(String path, Record record, TypeProperty property, String[] value)
                {
                    for (String handleString: value)
                    {
                        addPath(path, handleString);
                    }
                }

                @Override
                protected void handleReference(String path, Record record, TypeProperty property, String value)
                {
                    addPath(path, value);
                }

                private void addPath(String fromPath, String handleString)
                {
                    long handle = Long.parseLong(handleString);
                    if (handle != 0)
                    {
                        String referencedPath = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, handle);
                        if (referencedPath != null)
                        {
                            result.add(CollectionUtils.asPair(fromPath, referencedPath));
                        }
                    }
                }
            });

            return result;
        }
    }

    /**
     * Helper base class for "pull up" actions.
     */
    private class PullUpActionSupport extends PullPushActionSupport
    {
        protected final String ancestorKey;

        private PullUpActionSupport(String path, String ancestorKey)
        {
            super(path);
            this.ancestorKey = ancestorKey;
        }

        /**
         * Checks if our path can actually be pulled up, throwing an error if
         * it cannot.
         *
         * @return the path that the item would be pulled up to
         * @throws IllegalArgumentException if our path cannot be pulled up
         */
        public String ensurePullUp()
        {
            ensureMovable();

            // Specified ancestor must strictly be our ancestor.
            TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
            final boolean[] found = new boolean[]{false};
            node.forEachAncestor(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode node)
                {
                    if (node.getId().equals(ancestorKey))
                    {
                        found[0] = true;
                        return false;
                    }

                    return true;
                }
            }, true);

            if (!found[0])
            {
                throw new IllegalArgumentException("Specified ancestor '" + ancestorKey + "' is not an ancestor of this path's template owner");
            }

            // We must be able to insert the composite into the ancestor.
            // Requires checking the descendants of this ancestor -
            // ignoring the subtree where the composite is currently
            // defined.
            String scope = PathUtils.getPrefix(path, 1);
            String existingAncestorPath = configurationTemplateManager.findAncestorPath(path);
            if (existingAncestorPath != null)
            {
                throw new IllegalArgumentException("Path has an existing ancestor '" + existingAncestorPath + "'");
            }

            String ancestorPath = PathUtils.getPath(scope, ancestorKey, remainderPath);
            String ancestorParentPath = PathUtils.getParentPath(ancestorPath);
            ensurePathExists(ancestorParentPath, "Ancestor parent path");
            
            List<String> descendantPaths = configurationTemplateManager.getDescendantPaths(ancestorPath, true, false, false);
            List<String> existingDescendantPaths = configurationTemplateManager.getDescendantPaths(path, false, false, false);
            descendantPaths.removeAll(existingDescendantPaths);
            if (descendantPaths.size() > 0)
            {
                throw new IllegalArgumentException("Ancestor '" + ancestorKey + "' already has descendants " + descendantPaths + " that define this path");
            }

            // We cannot have any references to items defined in our template
            // owner that do not exist at the specified ancestor's level.
            Set<Pair<String, String>> referencedPaths = getReferencedPaths();
            for (Pair<String, String> pair: referencedPaths)
            {
                String referencedPath = pair.second;
                if (isUnder(referencedPath, templateOwnerPath) && !isUnder(referencedPath, path) && !configurationTemplateManager.pathExists(pullUpPath(referencedPath)))
                {
                    throw new IllegalArgumentException("Path contains reference to '" + referencedPath + "' which does not exist in ancestor '" + ancestorKey + "'");
                }
            }

            configurationSecurityManager.ensurePermission(ancestorPath, AccessManager.ACTION_CREATE);
            return ancestorPath;
        }

        private String pullUpPath(String path)
        {
            String[] elements = getPathElements(path);
            elements[1] = ancestorKey;
            return PathUtils.getPath(elements);
        }
    }

    /**
     * Action to test if a path can be pulled up.
     *
     * @see com.zutubi.tove.config.ConfigurationRefactoringManager#canPullUp(String, String)
     */
    private class CanPullUpAction extends PullUpActionSupport implements NullaryFunction<Boolean>
    {
        public CanPullUpAction(String path, String ancestorKey)
        {
            super(path, ancestorKey);
        }

        public Boolean process()
        {
            try
            {
                ensurePullUp();
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    /**
     * Action to pull up a path.
     *
     * @see com.zutubi.tove.config.ConfigurationRefactoringManager#pullUp(String, String)
     */
    private class PullUpAction extends PullUpActionSupport implements NullaryFunction<String>
    {
        public PullUpAction(String path, String ancestorKey)
        {
            super(path, ancestorKey);
        }

        public String process()
        {
            String insertPath;

            List<String> existingConcreteDescendants = configurationTemplateManager.getDescendantPaths(path, false, true, false);
            configurationTemplateManager.suspendInstanceCache();
            try
            {
                insertPath = ensurePullUp();

                String[] elements = getPathElements(insertPath);
                final String scope = elements[0];
                TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(scope);
                TemplateNode node = hierarchy.getNodeById(ancestorKey);

                CompositeType type = (CompositeType) configurationTemplateManager.getType(path);
                MutableRecord deepCopy = recordManager.select(path).copy(true, false);
                MutableRecord skeleton = configurationTemplateManager.createSkeletonRecord(type, deepCopy);
                if (configurationTemplateManager.isConcrete(path))
                {
                    // CIB-2673: external state should not be pulled up.
                    // Transfer and such state from the copy being inserted in
                    // the ancestor to the remaining skeleton at the original
                    // concrete path.
                    transferExternalState(type, deepCopy, skeleton);
                }
                
                configurationTemplateManager.addInheritedSkeletons(scope, PathUtils.getPath(2, elements), type, deepCopy, node, true);
                recordManager.insert(insertPath, deepCopy);

                skeleton.forEach(new DeepSkeletoniseFunction(path));

                // If the pulled up composite has a reference to within itself
                // it needs to be pulled up too.
                deepCopy = recordManager.select(insertPath).copy(true, true);
                deepCopy.forEach(new ReferenceUpdatingFunction(type, deepCopy, insertPath)
                {
                    @Override
                    protected String updateReference(String value)
                    {
                        long handle = Long.parseLong(value);
                        if (handle == 0)
                        {
                            return "0";
                        }
                        else
                        {
                            String referencedPath = recordManager.getPathForHandle(handle);
                            if (isUnder(referencedPath, PullUpAction.this.path))
                            {
                                referencedPath = PathUtils.getPath(scope, ancestorKey, PathUtils.getSuffix(referencedPath, 2));
                                handle = recordManager.select(referencedPath).getHandle();
                            }

                            return Long.toString(handle);
                        }
                    }
                });
                deepCopy.forEach(new DeepUpdateFunction(insertPath));

                // If any descendant had an internal reference to something
                // defined in the pulled up path it will need to be
                // canonicalised.
                node = node.findNodeById(PathUtils.getElement(path, 1));
                final CompositeType templateOwnerType = (CompositeType) configurationTemplateManager.getType(node.getPath());
                node.forEachDescendant(new Function<TemplateNode, Boolean>()
                {
                    public Boolean apply(TemplateNode node)
                    {
                        String descendantPath = node.getPath();
                        MutableRecord deepCopy = recordManager.select(descendantPath).copy(true, true);
                        deepCopy.forEach(new CanonicaliseReferencesFunction(templateOwnerType, deepCopy, descendantPath));
                        deepCopy.forEach(new DeepUpdateFunction(descendantPath));
                        return true;
                    }
                }, false, null);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Unable to pull up path '" + path + "': " + e.getMessage(), e);
            }
            finally
            {
                configurationTemplateManager.resumeInstanceCache();
            }

            List<String> newConcreteDescendants = configurationTemplateManager.getDescendantPaths(insertPath, false, true, false);
            newConcreteDescendants.removeAll(existingConcreteDescendants);
            configurationTemplateManager.raiseInsertEvents(newConcreteDescendants);

            return insertPath;
        }

        private void transferExternalState(CompositeType type, Record fromRecord, final MutableRecord toRecord)
        {
            fromRecord.forEach(new TypeAwareRecordWalkingFunction(type, fromRecord, "")
            {
                @Override
                protected void process(String path, Record record, Type type)
                {
                    if (type instanceof CompositeType)
                    {
                        for (TypeProperty property: ((CompositeType)type).getInternalProperties())
                        {
                            if (property.getAnnotation(ExternalState.class) != null)
                            {
                                Object value = record.get(property.getName());
                                if (value != null)
                                {
                                    Object to = toRecord.getPath(path);
                                    if (to != null && to instanceof MutableRecord)
                                    {
                                        ((MutableRecord) to).put(property.getName(), value);
                                    }

                                    ((MutableRecord) record).put(property.getName(), "0");
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Helper base class for "push down" actions.
     */
    private class PushDownActionSupport extends PullPushActionSupport
    {
        private PushDownActionSupport(String path)
        {
            super(path);
        }

        /**
         * Checks if our path can actually be pushed down to the given child,
         * throwing an error if it cannot.
         *
         * @param childKey key of the template child to test if we can push
         *                 down to
         * @return the path that the item would be pushed down to
         * @throws IllegalArgumentException if our path cannot be pushed down
         */
        public String ensurePushDown(String childKey)
        {
            ensureMovable();

            // Path must not be inherited.
            if (configurationTemplateManager.getTemplateParentRecord(path) != null)
            {
                throw new IllegalArgumentException("Path is inherited");
            }

            // Specified child must be a direct child.
            TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
            TemplateNode childNode = node.getChild(childKey);
            if (childNode == null)
            {
                throw new IllegalArgumentException("Specified child '" + childKey + "' is not a direct child of this path's template owner");
            }

            // The path must not be hidden in the child.
            String childPath = PathUtils.getPath(childNode.getPath(), remainderPath);
            if (!configurationTemplateManager.pathExists(childPath))
            {
                throw new IllegalArgumentException("Path is hidden in specified child '" + childKey + "'");
            }

            // We cannot have any references to items defined in our template
            // owner that do not exist at the specified child's level.  Nor can
            // there be references to within the item to push down from
            // elsewhere in the template owner.
            Set<Pair<String, String>> referencedPaths = getReferencedPaths();
            for (Pair<String, String> referencedPath: referencedPaths)
            {
                String fromPath = referencedPath.first;
                String toPath = referencedPath.second;
                if (isUnder(toPath, templateOwnerPath))
                {
                    if (isUnder(toPath, path))
                    {
                        if (!isUnder(fromPath, path))
                        {
                            throw new IllegalArgumentException("Reference from '" + fromPath + "' to within item to pull down that would become invalid");
                        }
                    }
                    else if (!configurationTemplateManager.pathExists(pushDownPath(toPath, childKey)))
                    {
                        throw new IllegalArgumentException("Path contains reference to '" + referencedPath + "' which does not exist in child '" + childKey + "'");
                    }
                }
            }

            return childPath;
        }

        private String pushDownPath(String path, String childKey)
        {
            String[] elements = getPathElements(path);
            elements[1] = childKey;
            return PathUtils.getPath(elements);
        }
    }

    /**
     * Action to test if a path can be pushed down.
     *
     * @see com.zutubi.tove.config.ConfigurationRefactoringManager#canPushDown(String, String)
     */
    private class CanPushDownAction extends PushDownActionSupport implements NullaryFunction<Boolean>
    {
        private String childKey;

        public CanPushDownAction(String path, String childKey)
        {
            super(path);
            this.childKey = childKey;
        }

        public Boolean process()
        {
            try
            {
                ensurePushDown(childKey);
                return true;
            }
            catch (IllegalArgumentException e)
            {
                return false;
            }
        }
    }

    /**
     * Action to push down a composite item in the template hierarchy.
     *
     * @see ConfigurationRefactoringManager#pushDown(String, Set<String>)
     */
    private class PushDownAction extends PushDownActionSupport implements NullaryFunction<Set<String>>
    {
        private Set<String> childKeys;

        private PushDownAction(String path, Set<String> childKeys)
        {
            super(path);
            this.childKeys = childKeys;
        }

        public Set<String> process()
        {
            validateChildKeys();

            // The easiest way is to push down to all children where the path
            // is not hidden, then later do a regular delete (with cleanup
            // tasks and events) for all children that are not included.
            Set<String> result = new HashSet<String>();
            Set<String> pathsToDelete = new HashSet<String>();

            configurationTemplateManager.suspendInstanceCache();
            try
            {
                Record pushDownRecord = recordManager.select(path);

                final String scope = PathUtils.getPrefix(path, 1);
                TemplateNode node = configurationTemplateManager.getTemplateNode(templateOwnerPath);
                CompositeType type = (CompositeType) configurationTemplateManager.getType(path);
                for (TemplateNode childNode: node.getChildren())
                {
                    final String childKey = childNode.getId();
                    String childPath = PathUtils.getPath(scope, childKey, remainderPath);
                    if (configurationTemplateManager.pathExists(childPath))
                    {
                        if (childKeys.contains(childKey))
                        {
                            result.add(childPath);
                        }
                        else
                        {
                            pathsToDelete.add(childPath);
                        }

                        pushDownToChild(scope, childPath, type, pushDownRecord);
                    }
                    else
                    {
                        cleanupHiddenKey(childPath);
                    }
                }

                // Go direct to the record manager to delete the pushed down
                // template records (no cleanup/events are required).
                recordManager.delete(path);
            }
            finally
            {
                configurationTemplateManager.resumeInstanceCache();
            }

            for (String deletePath: pathsToDelete)
            {
                configurationTemplateManager.delete(deletePath);
            }

            return result;
        }

        private void pushDownToChild(final String scope, final String childPath, final CompositeType type, Record pushDownRecord)
        {
            MutableRecord childRecordCopy = recordManager.select(childPath).copy(true, true);
            pushDownRecord.forEach(new PushDownValuesFunction(childRecordCopy));

            final String childKey = PathUtils.getElement(childPath, 1);
            childRecordCopy.forEach(new ReferenceUpdatingFunction(type, childRecordCopy, childPath)
            {
                @Override
                protected String updateReference(String value)
                {
                    long handle = Long.parseLong(value);
                    if (handle == 0)
                    {
                        return "0";
                    }
                    else
                    {
                        String referencedPath = recordManager.getPathForHandle(handle);
                        if (isUnder(referencedPath, PushDownAction.this.path))
                        {
                            referencedPath = PathUtils.getPath(scope, childKey, PathUtils.getSuffix(referencedPath, 2));
                            handle = recordManager.select(referencedPath).getHandle();
                        }

                        return Long.toString(handle);
                    }
                }
            });

            childRecordCopy.forEach(new DeepUpdateFunction(childPath));
        }

        private void validateChildKeys()
        {
            if (childKeys.isEmpty())
            {
                throw new IllegalArgumentException("At least one child must be specified");
            }

            for (String childKey: childKeys)
            {
                ensurePushDown(childKey);
            }
        }

        private void cleanupHiddenKey(String childPath)
        {
            // Hidden in this child, remove the hidden key from the
            // collection.
            String childCollectionPath = PathUtils.getParentPath(childPath);
            MutableRecord collectionClone = recordManager.select(childCollectionPath).copy(false, true);
            if (!TemplateRecord.restoreItem(collectionClone, PathUtils.getBaseName(childPath)))
            {
                throw new IllegalStateException("Did not find expected hidden key for path '" + childPath + "'");
            }
            
            recordManager.update(childCollectionPath, collectionClone);
        }

        private class PushDownValuesFunction implements GraphFunction<Record>
        {
            private Stack<MutableRecord> recordStack = new Stack<MutableRecord>();

            public PushDownValuesFunction(MutableRecord toRecord)
            {
                recordStack.push(toRecord);
            }

            public void push(String edge)
            {
                MutableRecord currentRecord = recordStack.peek();
                MutableRecord record = (MutableRecord) currentRecord.get(edge);
                if (record == null)
                {
                    // Must be hidden in child, remove the hidden key.
                    if (!TemplateRecord.restoreItem(currentRecord, edge))
                    {
                        throw new IllegalStateException("Did not find expected hidden key when child structure didn't match parent");
                    }

                    // We can harmlessly provide a dummy record to be processed
                    // (and then forgotten).
                    record = new MutableRecordImpl();
                }

                recordStack.push(record);
            }

            public void process(Record record)
            {
                MutableRecord toRecord = recordStack.peek();
                Set<String> toSimpleKeySet = toRecord.simpleKeySet();
                for (String key: record.simpleKeySet())
                {
                    if (!toSimpleKeySet.contains(key))
                    {
                        toRecord.put(key, record.get(key));
                    }
                }
            }

            public void pop()
            {
                recordStack.pop();
            }
        }
    }

    /**
     * Helper base class for move-related refactorings.
     */
    private abstract class MoveActionSupport implements NullaryFunction<MoveResult>
    {
        protected String path;
        protected String newTemplateParentKey;
        
        protected MoveResult result = new MoveResult();
        protected TemplateNode subtreeRoot;
        protected Record newTemplateParentRecord;
        protected Set<String> potentialAddedPaths = new HashSet<String>();

        public MoveActionSupport(String path, String newTemplateParentKey)
        {
            this.path = path;
            this.newTemplateParentKey = newTemplateParentKey;
        }

        /**
         * Validates input and collects information about a move, without
         * making any permanent changes.  Initialises fields to use during the
         * move.
         * 
         * @return true if the move is non-trivial, false if it is trivial
         *         (allowing an immediate return).
         */
        protected boolean previewMove()
        {
            Pair<String, String> moveItem = ensurePathRefersToNonRootTemplatedCollectionItem(path, "Path");
            ensureSpecified(newTemplateParentKey, "New template parent key");
            final String newTemplateParentPath = getPath(moveItem.first, newTemplateParentKey);
            ensurePathRefersToNonConcreteTemplatedCollectionItem(newTemplateParentPath, "New template parent path");

            if (newTemplateParentPath.equals(path))
            {
                throw new IllegalArgumentException(I18N.format("move.new.parent.is.self", newTemplateParentPath));
            }
            
            TemplateHierarchy hierarchy = configurationTemplateManager.getTemplateHierarchy(moveItem.first);
            subtreeRoot = hierarchy.getNodeById(moveItem.second);
            subtreeRoot.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode templateNode)
                {
                    if (templateNode.getId().equals(newTemplateParentKey))
                    {
                        throw new IllegalArgumentException(I18N.format("move.new.parent.is.descendant", newTemplateParentPath));
                    }
                    return true;
                }
            }, true, null);
            
            configurationSecurityManager.ensurePermission(newTemplateParentPath, AccessManager.ACTION_VIEW);
            subtreeRoot.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode templateNode)
                {
                    configurationSecurityManager.ensurePermission(templateNode.getPath(), AccessManager.ACTION_WRITE);
                    return true;
                }
            }, false, null);
            
            long existingTemplateParentHandle = configurationTemplateManager.getTemplateParentHandle(path, configurationTemplateManager.getRecord(path));
            String existingTemplateParentPath = recordManager.getPathForHandle(existingTemplateParentHandle);
            if (existingTemplateParentPath.equals(newTemplateParentPath))
            {
                // This is a no-op.  We could reject it, but that seems heavy
                // handed.  Programmatic use of moving should be able to safely
                // do a trivial move.
                return false;
            }

            newTemplateParentRecord = configurationTemplateManager.getRecord(newTemplateParentPath);
            subtreeRoot.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode node)
                {
                    TemplateRecord existingRecord = (TemplateRecord) configurationTemplateManager.getRecord(node.getPath());
                    if (node.isConcrete())
                    {
                        collectPotentialAdditions(node.getPath(), existingRecord, newTemplateParentRecord, potentialAddedPaths);
                    }
                    
                    findIncompatible(node.getPath(), existingRecord, newTemplateParentRecord, result);
                    return true;
                }
            }, false, null);
            
            return true;
        }
        
        private void collectPotentialAdditions(String path, Record detachedRecord, Record newTemplateParentRecord, Set<String> potentialAddedPaths)
        {
            for (String nestedKey: newTemplateParentRecord.nestedKeySet())
            {
                Object detachedValue = detachedRecord.get(nestedKey);
                String nestedPath = getPath(path, nestedKey);
                if (detachedValue != null && detachedValue instanceof Record)
                {
                    collectPotentialAdditions(nestedPath, (Record) detachedValue, (Record) newTemplateParentRecord.get(nestedKey), potentialAddedPaths);
                }
                else
                {
                    potentialAddedPaths.add(nestedPath);
                }
            }
        }

        private void findIncompatible(String path, Record existingRecord, Record newTemplateParentRecord, MoveResult result)
        {
            for (String nestedKey: existingRecord.nestedKeySet())
            {
                String nestedPath = getPath(path, nestedKey);
                Record nestedExistingRecord = (Record) existingRecord.get(nestedKey);

                Object templateParentValue = newTemplateParentRecord.get(nestedKey);
                if (templateParentValue != null && templateParentValue instanceof Record)
                {
                    Record templateParentNestedRecord = (Record) templateParentValue;
                    if (Objects.equal(templateParentNestedRecord.getSymbolicName(), nestedExistingRecord.getSymbolicName()))
                    {
                        findIncompatible(nestedPath, nestedExistingRecord, templateParentNestedRecord, result);
                    }
                    else
                    {
                        result.addDeletedPath(nestedPath);
                    }
                }
            }
        }
    }

    /**
     * Action to preview a move - to determine what the result would look like
     * without actually making changes.
     */
    private class PreviewMoveAction extends MoveActionSupport
    {
        public PreviewMoveAction(String path, String newTemplateParentPath)
        {
            super(path, newTemplateParentPath);
        }

        public MoveResult process()
        {
            previewMove();
            return result;
        }
    }
    
    /**
     * Action to move a templated collection item to a new location in the
     * hierarchy.
     */
    private class MoveAction extends MoveActionSupport
    {
        private MoveAction(String path, String newTemplateParentKey)
        {
            super(path, newTemplateParentKey);
        }

        public MoveResult process()
        {
            if (previewMove())
            {
                for (String path: result.getDeletedPaths())
                {
                    // The path may no longer exist, as it may have been
                    // deleted in an ancestor.
                    if (configurationTemplateManager.pathExists(path))
                    {
                        configurationTemplateManager.delete(path, false);
                    }
                }

                configurationTemplateManager.suspendInstanceCache();
                try
                {
                    // Detach all items we are moving (push down values).
                    subtreeRoot.forEachDescendant(new Function<TemplateNode, Boolean>()
                    {
                        public Boolean apply(TemplateNode node)
                        {
                            detach(node.getPath());
                            return true;
                        }
                    }, false, null);
                    
                    // Switch parents.
                    MutableRecord record = recordManager.select(path).copy(false, true);
                    configurationTemplateManager.setParentTemplate(record, newTemplateParentRecord.getHandle());
                    recordManager.update(path, record);
                    
                    // Heal all moved items.
                    subtreeRoot.forEachDescendant(new Function<TemplateNode, Boolean>()
                    {
                        public Boolean apply(TemplateNode node)
                        {
                            ConfigurationHealthReport report = configurationHealthChecker.healPath(node.getPath());
                            if (!report.isHealthy())
                            {
                                throw new IllegalStateException(I18N.format("move.heal.failed", node.getPath(), report.toString()));
                            }
    
                            return true;
                        }
                    }, false, null);
                    
                }
                finally
                {
                    configurationTemplateManager.resumeInstanceCache();
                }

                configurationTemplateManager.raiseInsertEvents(new LinkedList<String>(potentialAddedPaths));
            }
            
            return result;
        }

        private void detach(String path)
        {
            TemplateRecord record = (TemplateRecord) configurationTemplateManager.getRecord(path);
            MutableRecord flattened = record.flatten(true);
            detach(path, record.getMoi(), flattened);
        }

        private void detach(String path, Record existingRecord, MutableRecord detachedRecord)
        {
            // Recursively walks over the existing record, updating it where it
            // differs from the detached version (effectively pushes down
            // values that were previously scrubbed).
            if (!detachedRecord.shallowEquals(existingRecord))
            {
                recordManager.update(path, detachedRecord);
            }
            
            for (String nestedKey: existingRecord.nestedKeySet())
            {
                MutableRecord nestedDetachedRecord = (MutableRecord) detachedRecord.get(nestedKey);
                detach(getPath(path, nestedKey), (Record) existingRecord.get(nestedKey), nestedDetachedRecord);
            }
        }
    }
}
