package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.actions.Actions;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.format.Display;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.bean.ObjectFactory;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
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

    private ConfigurationRegistry configurationRegistry;

    private ObjectFactory objectFactory;

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

    private List<String> actions = new LinkedList<String>();

    private List<String> displayFields = new LinkedList<String>();

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

        if (configurationPersistenceManager.isPersistent(path))
        {
            record = configurationTemplateManager.getRecord(path);
        }

        parentPath = PathUtils.getParentPath(path);

        instance = configurationTemplateManager.getInstance(path);
        type = configurationTemplateManager.getType(path);
        targetType = type.getTargetType();

        if (targetType instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) targetType;
            targetSymbolicName = ctype.getSymbolicName();

            formHeading = PrototypeUtils.getFormHeading(ctype);
            simpleProperties = ctype.getSimplePropertyNames();
            nestedProperties = ctype.getNestedPropertyNames();

            // sort the nested properties.... this is a ui thing.
            final Collator collator = Collator.getInstance();
            Collections.sort(nestedProperties, new Comparator<String>()
            {
                public int compare(String o1, String o2)
                {
                    return collator.compare(o1, o2);
                }
            });

            extensions.addAll(((CompositeType) targetType).getExtensions());
            configurationCheckAvailable = configurationRegistry.getConfigurationCheckType(ctype) != null;
        }

        // determine the actions.
        if (!(type instanceof CollectionType))
        {
            Class actionHandler = ConventionSupport.getActions(type);
            if (actionHandler != null)
            {
                // do not show actions for template records.
                if (configurationTemplateManager.isConcrete(parentPath, record))
                {
                    Actions actionSupport = new Actions();
                    actionSupport.setObjectFactory(objectFactory);
                    actions = actionSupport.getActions(actionHandler, configurationTemplateManager.getInstance(path));
                }
            }

            Class displayHandler = ConventionSupport.getDisplay(type);
            if (displayHandler != null)
            {
                // do not show display fields for template records.
                if (configurationTemplateManager.isConcrete(parentPath, record))
                {
                    Display displaySupport = new Display();
                    displaySupport.setObjectFactory(objectFactory);
                    displayFields = displaySupport.getDisplayFields(displayHandler);
                }
            }
        }

        displayName = PrototypeUtils.getDisplayName(path, configurationTemplateManager);
    }

    public Object format(String fieldName)
    {
        Class displayHandler = ConventionSupport.getDisplay(targetType);
        if (displayHandler != null)
        {
            try
            {
                Display displaySupport = new Display();
                displaySupport.setObjectFactory(objectFactory);
                return displaySupport.format(displayHandler, fieldName, instance);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
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

    public List<String> getActions()
    {
        return actions;
    }

    public List<String> getDisplayFields()
    {
        return displayFields;
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

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
