package com.zutubi.pulse.master.tove.webwork;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.format.StateDisplayManager;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.model.ActionLinkComparator;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.*;
import com.zutubi.tove.config.api.ActionVariant;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.links.ConfigurationLink;
import com.zutubi.tove.links.LinkManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.Sort;
import com.zutubi.util.StringUtils;
import com.zutubi.util.adt.Pair;

import java.io.File;
import java.util.*;

import static com.google.common.collect.Collections2.transform;

/**
 * Analyses a configuration path, extracting information that is used to
 * render the path in the UI.
 */
public class ConfigurationUIModel
{
    private static final String DEFAULT_ICON = "generic";

    private RecordManager recordManager;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationProvider configurationProvider;
    private MasterConfigurationRegistry configurationRegistry;
    private LinkManager linkManager;
    private ActionManager actionManager;
    private StateDisplayManager stateDisplayManager;
    private StateDisplayRenderer stateDisplayRenderer;
    private SystemPaths systemPaths;

    private Record record;
    private Type type;

    private Type targetType;
    private String targetSymbolicName;

    private String path;
    private String[] pathElements;
    private String parentPath;
    private String[] parentPathElements;
    private String currentPath;
    private String formHeading;
    private String displayName;

    private List<String> simpleProperties;
    private List<String> nestedProperties = Collections.emptyList();
    private String collapsedCollection;
    private List<Pair<String, String>> nestedPropertyErrors = new LinkedList<Pair<String, String>>();

    private List<String> extensions = new LinkedList<String>();

    private List<ConfigurationLink> links = new LinkedList<ConfigurationLink>();
    private List<ActionLink> actions = new LinkedList<ActionLink>();
    private List<ActionLink> descendantActions = new LinkedList<ActionLink>();

    private List<String> displayFields = new LinkedList<String>();

    /**
     * A list of (-relative hierarchy depth, owner) pairs, sorted from closest
     * ancestor to most distant.
     */
    private List<Pair<Integer, String>> configuredAncestors = new LinkedList<Pair<Integer, String>>();
    /**
     * Note that this counts even descendants that the user has no permission
     * to view, which will be filtered out of the configuredDescendants tree.
     */
    private int configuredDescendantCount = 0;
    /**
     * A list of (relative hierarchy depth, owner) pairs, sorted in depth first
     * traversal order, with siblings sorted alphabetically.
     */
    private List<Pair<Integer, String>> configuredDescendants = new LinkedList<Pair<Integer, String>>();

    private boolean writable;
    private boolean embedded = false;
    private boolean displayMode = true;
    private boolean configurationCheckAvailable = false;

    private Configuration instance;

    public ConfigurationUIModel(String path)
    {
        if (!StringUtils.stringSet(path))
        {
            throw new IllegalArgumentException("Path must be provided.");
        }

        this.path = PathUtils.normalisePath(path);
    }

    public void analyse()
    {
        // load the type defined by the path.
        pathElements = PathUtils.getPathElements(path);
        if (pathElements.length == 0)
        {
            return;
        }

        parentPathElements = PathUtils.getParentPathElements(pathElements);
        parentPath = PathUtils.getPath(parentPathElements);
        currentPath = pathElements[pathElements.length - 1];

        parentPath = PathUtils.getParentPath(path);
        ComplexType parentType = null;
        if (parentPath != null)
        {
            parentType = configurationTemplateManager.getType(parentPath);
            if (ToveUtils.isEmbeddedCollection(parentType))
            {
                embedded = true;
                displayMode = false;
            }
        }

        type = configurationTemplateManager.getType(path);
        if (type == null)
        {
            // Could be a missing plugin, so try and provide a symbolic name
            // in that case to help diagnose.
            String message = "No type found for path '" + path + "', this could be due to a missing plugin.";
            try
            {
                Record value = recordManager.select(path);
                if (value != null)
                {
                    message += "  Path has symbolic name '" + value.getSymbolicName() + "'.";
                }
            }
            catch (Throwable e)
            {
                // Oh well, we tried.
            }
            
            throw new IllegalArgumentException(message);
        }

        targetType = type.getTargetType();

        if (targetType instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) targetType;
            targetSymbolicName = ctype.getSymbolicName();

            formHeading = ToveUtils.getFormHeading(ctype);
            simpleProperties = ctype.getSimplePropertyNames();

            extensions.addAll(transform(((CompositeType) targetType).getExtensions(), new Function<CompositeType, String>()
            {
                public String apply(CompositeType compositeType)
                {
                    return compositeType.getSymbolicName();
                }
            }));
            writable = configurationSecurityManager.hasPermission(path, AccessManager.ACTION_WRITE);
            configurationCheckAvailable = configurationRegistry.getConfigurationCheckType(ctype) != null;
        }

        if (configurationPersistenceManager.isPersistent(path))
        {
            record = configurationTemplateManager.getRecord(path);
            instance = configurationProvider.get(path, Configuration.class);
        }

        if (type instanceof CollectionType)
        {
            if (instance != null && targetType instanceof CompositeType)
            {
                CompositeType itemType = (CompositeType) targetType;
                @SuppressWarnings("unchecked")
                Collection<? extends Configuration> items = (Collection<? extends Configuration>) ((CollectionType) type).getItems(instance);
                displayFields = stateDisplayManager.getCollectionDisplayFields(itemType, items, instance);
            }
        }
        else
        {
            List<String> ancestorPaths = configurationTemplateManager.getAncestorPaths(path, true);
            configuredAncestors = new LinkedList<Pair<Integer, String>>();
            int i = ancestorPaths.size() - 1;
            for (String ancestorPath: ancestorPaths)
            {
                configuredAncestors.add(new Pair<Integer, String>(i, PathUtils.getPathElements(ancestorPath)[1]));
                i--;
            }
            
            // Is this path configured in any descendants?
            List<String> descendantPaths = configurationTemplateManager.getDescendantPaths(path, true, false, false);
            configuredDescendantCount = descendantPaths.size();
            determineConfiguredDescendants();

            determineActions(parentType);
            determineDescendantActions(descendantPaths);

            displayFields = stateDisplayManager.getDisplayFields(instance);
            
            if(instance == null)
            {
                if (configuredDescendantCount == 0 && !((CompositeType) type).isExtendable())
                {
                    resolveNested();
                }
            }
            else
            {
                resolveNested();
                
                for (String nested: nestedProperties)
                {
                    for(String error: instance.getFieldErrors(nested))
                    {
                        nestedPropertyErrors.add(new Pair<String, String>(nested, error));
                    }
                }

                links = linkManager.getLinks(instance);
            }
        }

        displayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
    }

    private void resolveNested()
    {
        collapsedCollection = ToveUtils.getCollapsedCollection(path, type, configurationSecurityManager);
        if (collapsedCollection == null)
        {
            nestedProperties = ToveUtils.getPathListing(path, type, configurationTemplateManager, configurationSecurityManager);
        }
    }
    
    private void determineConfiguredDescendants()
    {
        String elements[] = PathUtils.getPathElements(path);
        if (elements.length >= 2)
        {
            String ownerPath = PathUtils.getPath(0, 2, pathElements);
            TemplateNode node = configurationTemplateManager.getTemplateNode(ownerPath);
            if (node != null)
            {
                final String remainderPath = PathUtils.getPath(2, pathElements);
                final int topDepth = node.getDepth() + 1;
                node.forEachDescendant(new Function<TemplateNode, Boolean>()
                {
                    public Boolean apply(TemplateNode currentNode)
                    {
                        String descendantPath = remainderPath == null ? currentNode.getPath() : PathUtils.getPath(currentNode.getPath(), remainderPath);
                        if (configurationTemplateManager.pathExists(descendantPath) && configurationSecurityManager.hasPermission(descendantPath, AccessManager.ACTION_VIEW))
                        {
                            configuredDescendants.add(new Pair<Integer, String>(currentNode.getDepth() - topDepth, currentNode.getId()));
                        }
                        
                        return true;
                    }
                }, true, new NodeIdComparator());
            }
        }
    }

    private void determineActions(ComplexType parentType)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        List<String> actionNames = actionManager.getActions(instance, true, true);
        actionNames.remove(AccessManager.ACTION_VIEW);
        actionNames.remove(ConfigurationRefactoringManager.ACTION_CLONE);

        if (actionNames.size() > 0)
        {
            final String[] key = new String[]{ null };
            final Record[] parentRecord = new Record[]{ null };
            if (parentType != null && parentType instanceof MapType)
            {
                parentRecord[0] = configurationTemplateManager.getRecord(parentPath);
                key[0] = PathUtils.getBaseName(path);
            }

            actions = new ArrayList<>(actionNames.size());
            for (String actionName: actionNames)
            {
                List<ActionVariant> variants = null;
                if (instance != null)
                {
                    variants = actionManager.getVariants(actionName, instance);
                }

                if (variants == null)
                {
                    actions.add(ToveUtils.getActionLink(actionName, parentRecord[0], key[0], messages, systemPaths));
                }
                else
                {
                    for (ActionVariant variant: variants)
                    {
                        actions.add(new ActionLink(actionName, variant.getName(), ToveUtils.getActionIconName(actionName, systemPaths.getContentRoot()), variant.getName()));
                    }
                }
            }
        }
        else
        {
            actions = Collections.emptyList();
        }
    }

    private void determineDescendantActions(List<String> configuredDescendants)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        Set<String> actionSet = new HashSet<String>();
        for (String descendantPath: configuredDescendants)
        {
            Configuration instance = configurationTemplateManager.getInstance(descendantPath);
            if (instance != null && instance.isConcrete())
            {
                actionSet.addAll(actionManager.getActions(instance, false, false));
            }
        }

        for (String action: actionSet)
        {
            descendantActions.add(ToveUtils.getActionLink(action, messages, systemPaths.getContentRoot()));
        }

        Collections.sort(descendantActions, new ActionLinkComparator());
    }

    public Object format(String fieldName)
    {
        if (type instanceof CollectionType)
        {
            @SuppressWarnings("unchecked")
            Collection<? extends Configuration> items = (Collection<? extends Configuration>) ((CollectionType) type).getItems(instance);
            return stateDisplayRenderer.renderCollection(fieldName, (CompositeType) targetType, items, configurationProvider.get(parentPath, Configuration.class));
        }
        else
        {
            return stateDisplayRenderer.render(fieldName, instance);
        }
    }

    public boolean isConfigurationCheckAvailable()
    {
        return configurationCheckAvailable;
    }

    public String getPath()
    {
        return path;
    }

    public Configuration getInstance()
    {
        return instance;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getCurrentPath()
    {
        return currentPath;
    }

    public String[] getParentPathElements()
    {
        return parentPathElements;
    }

    public String[] getPathElements()
    {
        return pathElements;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getFormHeading()
    {
        return formHeading;
    }

    public Record getRecord()
    {
        return record;
    }

    public Type getType()
    {
        return type;
    }

    public Type getTargetType()
    {
        return targetType;
    }

    public List<String> getSimpleProperties()
    {
        return simpleProperties;
    }

    public List<String> getNestedProperties()
    {
        return nestedProperties;
    }

    public String getCollapsedCollection()
    {
        return collapsedCollection;
    }

    public boolean isPropertyInvalid(String property)
    {
        String propertyPath;
        if (collapsedCollection == null)
        {
            propertyPath = PathUtils.getPath(path, property);
        }
        else
        {
            propertyPath = PathUtils.getPath(path, collapsedCollection, property);
        }
        
        return configurationTemplateManager.pathExists(propertyPath) && !configurationTemplateManager.isDeeplyValid(propertyPath);
    }

    public List<Pair<String, String>> getNestedPropertyErrors()
    {
        return nestedPropertyErrors;
    }

    public List<String> getExtensions()
    {
        return extensions;
    }

    public String getTargetSymbolicName()
    {
        return targetSymbolicName;
    }

    public List<ConfigurationLink> getLinks()
    {
        return links;
    }

    public String getIconPath(String type, String name)
    {
        String path = composeIconPath(type, name);
        File iconFile = new File(systemPaths.getContentRoot(), path);
        if (iconFile.exists())
        {
            return path;
        }
        else
        {
            return composeIconPath(type, DEFAULT_ICON);
        }
    }

    private String composeIconPath(String type, String name)
    {
        return StringUtils.join("/", "images", "config", type, name + ".gif");
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public List<ActionLink> getDescendantActions()
    {
        return descendantActions;
    }

    public List<String> getDisplayFields()
    {
        return displayFields;
    }

    public List<Pair<Integer, String>> getConfiguredAncestors()
    {
        return configuredAncestors;
    }

    public int getConfiguredDescendantCount()
    {
        return configuredDescendantCount;
    }

    public List<Pair<Integer, String>> getConfiguredDescendants()
    {
        return configuredDescendants;
    }

    public boolean isWritable()
    {
        return writable;
    }

    public boolean isEmbedded()
    {
        return embedded;
    }

    public boolean isDisplayMode()
    {
        return displayMode;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationRegistry(MasterConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setLinkManager(LinkManager linkManager)
    {
        this.linkManager = linkManager;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setStateDisplayManager(StateDisplayManager stateDisplayManager)
    {
        this.stateDisplayManager = stateDisplayManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }

    public void setStateDisplayRenderer(StateDisplayRenderer stateDisplayRenderer)
    {
        this.stateDisplayRenderer = stateDisplayRenderer;
    }

    private static class NodeIdComparator implements Comparator<TemplateNode>
    {
        private static final Sort.StringComparator DELEGATE = new Sort.StringComparator();
        
        public int compare(TemplateNode n1, TemplateNode n2)
        {
            return DELEGATE.compare(n1.getId(), n2.getId());
        }
    }
}
