package com.zutubi.prototype.webwork;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.config.annotations.Classification;
import com.zutubi.config.annotations.Listing;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.config.ConfigurationSecurityManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.freemarker.BaseNameMethod;
import com.zutubi.prototype.freemarker.GetTextMethod;
import com.zutubi.prototype.freemarker.ValidIdMethod;
import com.zutubi.prototype.model.Form;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.freemarker.FreemarkerConfigurationFactoryBean;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.webwork.mapping.PulseActionMapper;
import com.zutubi.util.*;
import freemarker.core.DelegateBuiltin;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Webwork environment specific prototype utility methods.
 */
public class PrototypeUtils
{
    private static final String KEY_LABEL = "label";
    private static final String KEY_FORM_HEADING = "form.heading";
    private static final String KEY_TABLE_HEADING = "table.heading";

    private static final String[] EMPTY_ARRAY = {};

    public static String getConfigURL(String path, String action, String submitField)
    {
        return getConfigURL(path, action, submitField, null);
    }

    public static String getConfigURL(String path, String action, String submitField, String namespace)
    {
        String result = (namespace != null) ? namespace : PulseActionMapper.ADMIN_NAMESPACE;
        if (path != null)
        {
            result = StringUtils.join("/", true, true, result, path);
        }

        result = PathUtils.normalizePath(result);
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

    /**
     * Convert the parameters from the HTTP post into a record, according to the type definition.
     * <p/>
     * NOTE: This method does not do any real type conversion. Instead, it 'unwraps' data that has been
     * wrapped in String[]s'.
     *
     * @param type       instance that defines the data contained in the parameters map.
     * @param parameters map that contains the http parameters to be converted into a record.
     * @return a record instance containing the parameter data that applies to the map.
     */
    public static MutableRecord toRecord(CompositeType type, Map<String, String[]> parameters)
    {
        MutableRecord record = new MutableRecordImpl();
        record.setSymbolicName(type.getSymbolicName());

        for (TypeProperty property : type.getProperties())
        {
            String propertyName = property.getName();

            // this is a read only property.
            if (!property.isWriteable())
            {
                continue;
            }

            String[] parameterValue = parameters.get(propertyName);
            if (parameterValue == null)
            {
                parameterValue = parameters.get(propertyName + ".default");
                if (parameterValue == null)
                {
                    continue;
                }
            }

            if (Collection.class.isAssignableFrom(property.getClazz()))
            {
                if(parameterValue.length == 1 && parameterValue[0].length() == 0)
                {
                    // This indicates an empty array: something the UI is
                    // incapable of sending directly for custom components.
                    parameterValue = EMPTY_ARRAY;
                }
                record.put(propertyName, parameterValue);
            }
            else
            {
                record.put(propertyName, parameterValue[0]);
            }
        }
        return record;
    }

    public static boolean isFolder(String path, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        Type type = configurationTemplateManager.getType(path);
        return (type instanceof MapType) || getPathListing(path, type, configurationTemplateManager, configurationSecurityManager).size() > 0;
    }

    public static boolean isLeaf(String path, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        return !isFolder(path, configurationTemplateManager, configurationSecurityManager);
    }

    @SuppressWarnings({"unchecked"})
    public static List<String> getPathListing(String path, Type type, ConfigurationTemplateManager configurationTemplateManager, ConfigurationSecurityManager configurationSecurityManager)
    {
        List<String> listing = Collections.EMPTY_LIST;

        if (path.length() == 0)
        {
            listing = configurationTemplateManager.getRootListing();
            Collections.sort(listing, new Sort.StringComparator());
        }
        else if (type instanceof MapType)
        {
            Record record = configurationTemplateManager.getRecord(path);
            if (record != null)
            {
                listing = new LinkedList<String>(((CollectionType) type).getOrder(record));
                Collections.sort(listing, new Sort.StringComparator());
            }
        }
        else if (type instanceof CompositeType)
        {
            listing = getSortedNestedProperties(path, (CompositeType) type, configurationTemplateManager);
        }

        return configurationSecurityManager.filterPaths(path, listing, AccessManager.ACTION_VIEW);
    }

    public static List<String> getSortedNestedProperties(final String path, final CompositeType type, final ConfigurationTemplateManager configurationTemplateManager)
    {
        List<String> result = new LinkedList<String>();
        List<String> nestedProperties = type.getNestedPropertyNames();

        // First process the order defined in @Listing (if any)
        Listing annotation = type.getAnnotation(Listing.class);
        if(annotation != null)
        {
            String[] definedOrder = annotation.order();
            for(String property: definedOrder)
            {
                if(nestedProperties.remove(property))
                {
                    result.add(property);
                }
            }
        }

        // Remaining properties are sorted alphabetically by display name
        if(nestedProperties.size() > 0)
        {
            final Record value = configurationTemplateManager.getRecord(path);

            // Get property/display name pairs
            List<Pair<String, String>> propertyDisplayPairs = CollectionUtils.map(nestedProperties, new Mapping<String, Pair<String, String>>()
            {
                public Pair<String, String> map(String s)
                {
                    String propertyPath = PathUtils.getPath(path, s);
                    ComplexType propertyType = configurationTemplateManager.getType(propertyPath);
                    return new Pair<String, String>(s, getDisplayName(propertyPath, propertyType, type, (Record) value.get(s)));
                }
            });

            // Sort by display name
            final Sort.StringComparator comp = new Sort.StringComparator();
            Collections.sort(propertyDisplayPairs, new Comparator<Pair<String, String>>()
            {
                public int compare(Pair<String, String> o1, Pair<String, String> o2)
                {
                    return comp.compare(o1.second, o2.second);
                }
            });

            // Pull out property names and add to result
            CollectionUtils.map(propertyDisplayPairs, new Mapping<Pair<String, String>, String>()
            {
                public String map(Pair<String, String> pair)
                {
                    return pair.first;
                }
            }, result);
        }

        return result;
    }

    public static List<String> getEmbeddedCollections(CompositeType ctype)
    {
        List<String> result = new LinkedList<String>();
        for(TypeProperty property: ctype.getProperties())
        {
            if(isEmbeddedCollection(property.getType()))
            {
                result.add(property.getName());
            }
        }

        return result;
    }

    public static boolean isEmbeddedCollection(Type type)
    {
        return type instanceof ListType && type.getTargetType() instanceof ComplexType;
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
        if (TextUtils.stringSet(path))
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
                String key = baseName + ".label";
                if (messages.isKeyDefined(key))
                {
                    result = messages.format(key);
                }
                else
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

    public static String getFormHeading(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        if(messages.isKeyDefined(KEY_FORM_HEADING))
        {
            return messages.format(KEY_FORM_HEADING);
        }
        else
        {
            // Default is just the label.
            return messages.format(KEY_LABEL);
        }
    }

    public static String getTableHeading(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        if(messages.isKeyDefined(KEY_TABLE_HEADING))
        {
            return messages.format(KEY_TABLE_HEADING);
        }
        else
        {
            return getPluralLabel(messages);
        }
    }

    public static String getPluralLabel(Type type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        return getPluralLabel(messages);
    }

    private static String getPluralLabel(Messages messages)
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

    public static void mapErrors(Configuration instance, ValidationAware validationAware, String fieldSuffix)
    {
        for(String instanceError: instance.getInstanceErrors())
        {
            validationAware.addActionError(instanceError);
        }

        for(Map.Entry<String, List<String>> fieldEntry: instance.getFieldErrors().entrySet())
        {
            String fieldName = fieldSuffix == null ? fieldEntry.getKey() : fieldEntry.getKey() + fieldSuffix;
            for(String error: fieldEntry.getValue())
            {
                validationAware.addFieldError(fieldName, error);
            }
        }
    }

    public static String getClassification(ComplexType type)
    {
        Classification classification = type.getTargetType().getAnnotation(Classification.class);
        if(classification != null)
        {
            if(type instanceof CompositeType)
            {
                if(TextUtils.stringSet(classification.single()))
                {
                    return classification.single();
                }
            }
            else
            {
                if(TextUtils.stringSet(classification.collection()))
                {
                    return classification.collection();
                }
            }
        }

        return type instanceof CompositeType ? "composite" : "collection";
    }

    public static String getIconCls(ComplexType type)
    {
        return "config-" + getClassification(type) + "-icon";
    }

    public static Map<String, Object> initialiseContext(Class clazz)
    {
        Map<String, Object> context = new HashMap<String, Object>();
        return populateContext(clazz, context);
    }

    public static Map<String, Object> populateContext(Class clazz, Map<String, Object> context)
    {
        Messages messages = Messages.getInstance(clazz);
        context.put("i18nText", new GetTextMethod(messages));
        context.put("baseName", new BaseNameMethod());
        context.put("validId", new ValidIdMethod());

        // validation support:
        OgnlValueStack stack = ActionContext.getContext().getValueStack();
        context.put("fieldErrors", stack.findValue("fieldErrors"));

        // provide some syntactic sweetener by linking the i18n text method to the ?i18n builtin function.
        DelegateBuiltin.conditionalRegistration("i18n", "i18nText");
        DelegateBuiltin.conditionalRegistration("baseName", "baseName");
        DelegateBuiltin.conditionalRegistration("id", "validId");
        return context;
    }

    public static void renderForm(Map<String, Object> context, Form form, Class i18nClazz, Writer writer, MasterConfigurationManager configurationManager) throws TemplateException, IOException
    {
        populateContext(i18nClazz, context);
        context.put("form", form);
        freemarker.template.Configuration configuration = FreemarkerConfigurationFactoryBean.createConfiguration(i18nClazz, configurationManager);
        Template template = configuration.getTemplate("prototype/xhtml/form.ftl");
        template.process(context, writer);
    }
}
