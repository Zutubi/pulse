package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.classification.ClassificationManager;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.TemplateRecord;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Webwork environment specific tove utility methods.
 */
public class ToveUtils
{
    public static final String SUPPRESSED_PASSWORD = "[suppressed password]";

    public static String getConfigURL(String path, String action, String submitField, String namespace)
    {
        String result = (namespace != null) ? namespace : PulseActionMapper.ADMIN_NAMESPACE;
        if (path != null)
        {
            String[] elements = PathUtils.getPathElements(path);
            for (String element: elements)
            {
                result += "/";
                result += WebUtils.uriComponentEncode(element);
            }
        }

        result = PathUtils.normalisePath(result);
        if (action != null && !action.equals("display") || submitField != null)
        {
            result = result + "?" + action;
        }

        if (submitField != null)
        {
            result = result + "=" + submitField;
        }

        return result;
    }

    public static String getDisplayName(String path, ConfigurationTemplateManager configurationTemplateManager)
    {
        // Cases:
        //   - Empty path: empty name
        //   - Scope (single element path): the path itself, which is the
        //     name of the scope
        //   - Transient path: look for label in <type>.properties, otherwise
        //     just use the basename
        //   - Persistent path: see below
        if (StringUtils.stringSet(path))
        {
            String parentPath = PathUtils.getParentPath(path);
            if (parentPath == null)
            {
                // A scope, just return the scope name;
                return path;
            }
            else
            {
                if (configurationTemplateManager.isPersistent(path))
                {
                    return getDisplayName(path, configurationTemplateManager.getType(path), configurationTemplateManager.getType(parentPath), configurationTemplateManager.getRecord(path));
                }
                else
                {
                    ComplexType type = configurationTemplateManager.getType(path);
                    Messages messages = Messages.getInstance(type.getClazz());
                    if(messages.isKeyDefined("label"))
                    {
                        return messages.format("label");
                    }
                    else
                    {
                        return PathUtils.getBaseName(path);
                    }
                }
            }
        }
        else
        {
            // Empty path, empty name
            return path;
        }
    }

    public static String getDisplayName(String path, ComplexType type, ComplexType parentType, Record value)
    {
        // One of:
        //   - the id, if this object is within a map
        //   - the value of the first defined i18n key if this is a composite
        //     property:
        //       <parent type>.properties: <property>.label
        //       <type>.properties: label (if singular and configured)
        //       <property type>.properties: label.plural (if a collection)
        //       <property type>.properties: label (auto-pluralised if a collection)
        String result = null;
        String baseName = PathUtils.getBaseName(path);

        if (parentType != null)
        {
            if (parentType instanceof MapType)
            {
                result = (String) value.get(((MapType) parentType).getKeyProperty());
            }
            else
            {
                Messages messages = Messages.getInstance(parentType.getClazz());
                String key = baseName + ConventionSupport.I18N_KEY_SUFFIX_LABEL;
                if (messages.isKeyDefined(key))
                {
                    result = messages.format(key);
                }
                else if(type != null)
                {
                    if (type instanceof CollectionType)
                    {
                        result = getPluralLabel(type.getTargetType());
                    }
                    else
                    {
                        messages = Messages.getInstance(type.getClazz());
                        result = messages.format("label");
                    }
                }
            }
        }

        if (result == null)
        {
            result = baseName;
        }

        return result;
    }

    /**
     * Evaluates the order of fields for a form based on a possibly-
     * incomplete declared field order.  Undeclared fields are added to the
     * end of the result in no specific order.  Declared fields not in
     * allFields are ignored (this can happen, for example, in a wizard where
     * some fields are ignored).
     *
     * @param declaredOrder field ordering explicitly declared so far (may be
     *                      null)
     * @param allFields     all fields for the form
     * @return the order that the forms should appear on a form - some
     *         permutation of allFields.
     */
    public static List<String> evaluateFieldOrder(Iterable<String> declaredOrder, Collection<String> allFields)
    {
        LinkedList<String> ordered = new LinkedList<>();
        if (declaredOrder != null)
        {
            for (String declared: declaredOrder)
            {
                if (allFields.contains(declared))
                {
                    ordered.add(declared);
                }
            }
        }

        if (ordered.size() != allFields.size())
        {
            // Add those fields that we have missed to the end of the list.
            for (String field: allFields)
            {
                if (!ordered.contains(field))
                {
                    ordered.addLast(field);
                }
            }
        }

        return ordered;
    }

    public static String getPluralLabel(Type type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        return getPluralLabel(messages);
    }

    public static String getPluralLabel(Messages messages)
    {
        if (messages.isKeyDefined("label.plural"))
        {
            return messages.format("label.plural");
        }
        else
        {
            // Auto-pluralise
            return StringUtils.pluralise(messages.format("label"));
        }
    }

    public static String getIconCls(String path, ClassificationManager classificationManager)
    {
        return "config-" + classificationManager.classify(path) + "-icon";
    }

    /**
     * Creates an action link for a given action on the given data.  The link
     * includes extra UI decoration, e.g. the icon and potential
     * transformation of "delete" into "hide" for display purposes.  In order
     * to determine if the "delete" action should be "hide", the parent record
     * (i.e. the map) is required when getting action links for map items.
     *
     * @param actionName   action to create the link for
     * @param parentRecord if the parent record is a map, that map record,
     *                     otherwise null
     * @param key          if the parent record is a map, the map key of the
     *                     item that the action applies to, otherwise null
     * @param messages     used to format UI labels
     * @param systemPaths  used to locate icons
     * @return details of an action link for UI display
     */
    public static ActionLink getActionLink(String actionName, Record parentRecord, String key, Messages messages, SystemPaths systemPaths)
    {
        if (key != null && !parentRecord.isCollection())
        {
            throw new IllegalArgumentException("Can only specify a key for map items.  Did you pass the parent record?");
        }
        
        String action = actionName;
        if(actionName.equals(AccessManager.ACTION_DELETE) && parentRecord instanceof TemplateRecord)
        {
            TemplateRecord templateRecord = (TemplateRecord) parentRecord;
            TemplateRecord templateParent = templateRecord.getParent();
            if(templateParent != null && key != null && templateParent.getOwner(key) != null)
            {
                actionName = "hide";
            }
        }

        File contentRoot = systemPaths.getContentRoot();
        return getActionLink(action, actionName, messages, contentRoot);
    }

    public static ActionLink getActionLink(String action, Messages messages, File contentRoot)
    {
        return getActionLink(action, action, messages, contentRoot);
    }

    private static ActionLink getActionLink(String action, String actionName, Messages messages, File contentRoot)
    {
        return new ActionLink(action, format(messages, actionName + ConventionSupport.I18N_KEY_SUFFIX_LABEL), getActionIconName(actionName, contentRoot));
    }

    public static String getActionIconName(String actionName, File contentRoot)
    {
        File iconFile = new File(contentRoot, FileSystemUtils.composeFilename("images", "config", "actions", actionName + ".gif"));
        return iconFile.exists() ? actionName : "generic";
    }

    public static String format(Messages messages, String key)
    {
        String value = messages.format(key);
        if(value == null)
        {
            value = key;
        }
        return value;
    }

    public static String getStatusIcon(BuildResult result)
    {
        return getStatusIcon(result.getState());
    }

    public static String getStatusIcon(ResultState state)
    {
        switch (state)
        {
            case SUCCESS:
                return "accept.gif";
            case WARNINGS:
                return "error.gif";
            case ERROR:
            case FAILURE:
            case TERMINATED:
                return "exclamation.gif";
            case IN_PROGRESS:
                return "inprogress.gif";
            case PENDING:
                return "hourglass.gif";
            case TERMINATING:
                return "stop.gif";
            default:
                return "help.gif";
        }
    }

    public static String getStatusClass(BuildResult result)
    {
        return getStatusClass(result.getState());
    }

    public static String getStatusClass(ResultState state)
    {
        switch (state)
        {
            case SUCCESS:
                return "success";
            case WARNINGS:
                return "warning";
            case ERROR:
            case FAILURE:
                return "failure";
            default:
                return "content";
        }
    }

    /**
     * Suppresses the values of all password fields in the given record in
     * preparation for it to be handed out via a UI. 
     * 
     * @param record the record to suppress password values within
     * @param type   the type of the given record
     * @param deep   set to true to recursively suppresses values in child
     *               records
     */
    public static void suppressPasswords(MutableRecord record, ComplexType type, boolean deep)
    {
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            for (String propertyName: compositeType.getSimplePropertyNames())
            {
                if (record.containsKey(propertyName) && isPasswordProperty(compositeType, propertyName))
                {
                    record.put(propertyName, SUPPRESSED_PASSWORD);
                }
            }
        }
        
        if (deep)
        {
            for (String nestedKey: record.nestedKeySet())
            {
                MutableRecord childRecord = (MutableRecord) record.get(nestedKey);
                suppressPasswords(childRecord, (ComplexType) type.getActualPropertyType(nestedKey, childRecord), true);
            }
        }
    }

    /**
     * Restores the values of all unchanged password fields in the given record
     * from the given original record.  An unchanged field will have the
     * placeholder value used by {@link #suppressPasswords(com.zutubi.tove.type.record.MutableRecord, com.zutubi.tove.type.ComplexType, boolean)}.
     * 
     * @param originalRecord the original record, with original password values
     * @param newRecord      the input record in which to unsuppress passwords
     * @param type           the type of the given records
     * @param deep           set to true to recursively unsuppresses values in
     *                       child records
     */
    public static void unsuppressPasswords(Record originalRecord, MutableRecord newRecord, ComplexType type, boolean deep)
    {
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            for (String propertyName: compositeType.getSimplePropertyNames())
            {
                if (isPasswordProperty(compositeType, propertyName) && SUPPRESSED_PASSWORD.equals(newRecord.get(propertyName)))
                {
                    newRecord.put(propertyName, originalRecord.get(propertyName));
                }
            }
        }
        
        if (deep)
        {
            for (String nestedKey: newRecord.nestedKeySet())
            {
                MutableRecord childNewRecord = (MutableRecord) newRecord.get(nestedKey);
                ComplexType childNewType = (ComplexType) type.getActualPropertyType(nestedKey, childNewRecord);
                Record childOriginalRecord = (Record) originalRecord.get(nestedKey);
                if (childOriginalRecord != null)
                {
                    ComplexType childOriginalType = (ComplexType) type.getActualPropertyType(nestedKey, childOriginalRecord);
                    if (childNewType.equals(childOriginalType))
                    {
                        unsuppressPasswords(childOriginalRecord, childNewRecord, childNewType, true);
                    }
                }
            }
        }
    }
    
    private static boolean isPasswordProperty(CompositeType type, String propertyName)
    {
        return type.getProperty(propertyName).getAnnotation(Password.class) != null;
    }
}
