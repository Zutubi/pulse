package com.zutubi.prototype.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.prototype.config.*;
import com.zutubi.prototype.format.StateDisplayManager;
import com.zutubi.prototype.model.ActionLink;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.TextUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Analyses a configuration path, extracting information that is used to
 * render the path in the UI.
 */
public class ConfigurationUIModel
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationSecurityManager configurationSecurityManager;
    private ConfigurationProvider configurationProvider;
    private ConfigurationRegistry configurationRegistry;
    private ActionManager actionManager;
    private StateDisplayManager stateDisplayManager;
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
    private List<String> nestedProperties;
    private List<String> extensions = new LinkedList<String>();

    private List<ActionLink> actions = new LinkedList<ActionLink>();

    private List<String> displayFields = new LinkedList<String>();

    private List<String> configuredDescendents = Collections.EMPTY_LIST;

    private boolean writable;
    private boolean embedded = false;
    private boolean displayMode = true;
    private boolean configurationCheckAvailable = false;

    private Configuration instance;

    public ConfigurationUIModel(String path)
    {
        if (!TextUtils.stringSet(path))
        {
            throw new IllegalArgumentException("Path must be provided.");
        }

        this.path = PathUtils.normalizePath(path);

        ComponentContext.autowire(this);

        analyse();
    }

    private void analyse()
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
        if (parentPath != null)
        {
            ComplexType parentType = configurationTemplateManager.getType(parentPath);
            if (PrototypeUtils.isEmbeddedCollection(parentType))
            {
                embedded = true;
                displayMode = false;
            }
        }

        type = configurationTemplateManager.getType(path);
        targetType = type.getTargetType();

        nestedProperties = PrototypeUtils.getPathListing(path, type, configurationTemplateManager, configurationSecurityManager);

        if (targetType instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) targetType;
            targetSymbolicName = ctype.getSymbolicName();

            formHeading = PrototypeUtils.getFormHeading(ctype);
            simpleProperties = ctype.getSimplePropertyNames();

            extensions.addAll(CollectionUtils.map(((CompositeType) targetType).getExtensions(), new Mapping<CompositeType, String>()
            {
                public String map(CompositeType compositeType)
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

        if (!(type instanceof CollectionType))
        {
            // determine the actions.
            final Messages messages = Messages.getInstance(type.getClazz());
            List<String> actionNames = actionManager.getActions(instance, true);
            actionNames.remove(AccessManager.ACTION_VIEW);
            actionNames.remove(AccessManager.ACTION_CLONE);
            actions = CollectionUtils.map(actionNames, new Mapping<String, ActionLink>()
            {
                public ActionLink map(String actionName)
                {
                    return PrototypeUtils.getActionLink(actionName, record, null, messages, systemPaths);
                }
            });

            displayFields = stateDisplayManager.getDisplayFields(instance);
            
            if(instance == null)
            {
                // Is this path configured in any descendents?
                List<String> descendentPaths = configurationTemplateManager.getDescendentPaths(path, true, false, false);
                configuredDescendents = CollectionUtils.map(descendentPaths, new Mapping<String, String>()
                {
                    public String map(String s)
                    {
                        return PathUtils.getPathElements(s)[1];
                    }
                });
            }
        }

        displayName = PrototypeUtils.getDisplayName(path, configurationTemplateManager);
    }

    public Object format(String fieldName)
    {
        return stateDisplayManager.format(fieldName, instance);
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

    public List<String> getExtensions()
    {
        return extensions;
    }

    public String getTargetSymbolicName()
    {
        return targetSymbolicName;
    }

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public List<String> getDisplayFields()
    {
        return displayFields;
    }

    public List<String> getConfiguredDescendents()
    {
        return configuredDescendents;
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

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
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
}
