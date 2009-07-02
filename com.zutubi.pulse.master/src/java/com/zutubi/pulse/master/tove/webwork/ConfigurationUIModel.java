package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.format.StateDisplayManager;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.links.ConfigurationLink;
import com.zutubi.tove.links.LinkManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.*;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
    private ConfigurationRegistry configurationRegistry;
    private LinkManager linkManager;
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
    private List<Pair<String, String>> nestedPropertyErrors = new LinkedList<Pair<String, String>>();

    private List<String> extensions = new LinkedList<String>();

    private List<ConfigurationLink> links = new LinkedList<ConfigurationLink>();
    private List<ActionLink> actions = new LinkedList<ActionLink>();

    private List<String> displayFields = new LinkedList<String>();

    private List<String> configuredDescendents = new LinkedList<String>();

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

        nestedProperties = ToveUtils.getPathListing(path, type, configurationTemplateManager, configurationSecurityManager);

        if (targetType instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) targetType;
            targetSymbolicName = ctype.getSymbolicName();

            formHeading = ToveUtils.getFormHeading(ctype);
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

            if (instance != null)
            {
                for (String nested: nestedProperties)
                {
                    for(String error: instance.getFieldErrors(nested))
                    {
                        nestedPropertyErrors.add(new Pair<String, String>(nested, error));
                    }
                }
            }
        }

        if (!(type instanceof CollectionType))
        {
            determineActions(parentType);

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
            else
            {
                links = linkManager.getLinks(instance);
            }
        }

        displayName = ToveUtils.getDisplayName(path, configurationTemplateManager);
    }

    private void determineActions(ComplexType parentType)
    {
        final Messages messages = Messages.getInstance(type.getClazz());
        List<String> actionNames = actionManager.getActions(instance, true);
        actionNames.remove(AccessManager.ACTION_VIEW);
        actionNames.remove(AccessManager.ACTION_CLONE);

        if (actionNames.size() > 0)
        {
            final String[] key = new String[]{ null };
            final Record[] parentRecord = new Record[]{ null };
            if (parentType != null && parentType instanceof MapType)
            {
                parentRecord[0] = configurationTemplateManager.getRecord(parentPath);
                key[0] = PathUtils.getBaseName(path);
            }

            actions = CollectionUtils.map(actionNames, new Mapping<String, ActionLink>()
            {
                public ActionLink map(String actionName)
                {
                    return ToveUtils.getActionLink(actionName, parentRecord[0], key[0], messages, systemPaths);
                }
            });
        }
        else
        {
            actions = Collections.emptyList();
        }
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

    public boolean isPropertyInvalid(String property)
    {
        String propertyPath = PathUtils.getPath(path, property);
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
}
