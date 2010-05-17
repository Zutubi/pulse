package com.zutubi.pulse.acceptance.forms;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.tove.annotations.Field;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.InstanceOfPredicate;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.AnnotationUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base for forms that are based off a configuration class, where
 * the class is available (i.e. not a plugin).  The form and field names are
 * automatically determined from the class.
 */
public class ConfigurationForm extends SeleniumForm
{
    public static final String ANNOTATION_INHERITED = "inherited";

    private static final Logger LOG = Logger.getLogger(ConfigurationForm.class);

    private Class<? extends Configuration> configurationClass;
    private List<FieldInfo> fields = new LinkedList<FieldInfo>();

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass)
    {
        this(browser, configurationClass, true);
    }

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass, boolean ajax)
    {
        this(browser, configurationClass, ajax, false);
    }

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass, boolean ajax, boolean inherited)
    {
        this(browser, configurationClass, ajax, inherited, false);
    }

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass, boolean ajax, boolean inherited, boolean wizard)
    {
        super(browser, ajax, inherited);
        this.configurationClass = configurationClass;
        analyzeFields(wizard);
    }

    public String getFormName()
    {
        return configurationClass.getName();
    }

    public String[] getFieldNames()
    {
        return CollectionUtils.mapToArray(fields, new Mapping<FieldInfo, String>()
        {
            public String map(FieldInfo fieldInfo)
            {
                return fieldInfo.name;
            }
        }, new String[fields.size()]);
    }

    public int[] getFieldTypes()
    {
        int[] types = new int[fields.size()];
        int i = 0;
        for (FieldInfo fieldInfo: fields)
        {
            types[i++] = fieldInfo.type;
        }

        return types;
    }

    private void analyzeFields(boolean ignoreWizard)
    {
        try
        {
            Form formAnnotation = configurationClass.getAnnotation(Form.class);
            final String[] fieldNames = formAnnotation == null ? getFieldNames() : formAnnotation.fieldOrder();
            BeanInfo beanInfo = Introspector.getBeanInfo(configurationClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (final String fieldName : fieldNames)
            {
                PropertyDescriptor property = CollectionUtils.find(propertyDescriptors, new Predicate<PropertyDescriptor>()
                {
                    public boolean satisfied(PropertyDescriptor propertyDescriptor)
                    {
                        return propertyDescriptor.getName().equals(fieldName);
                    }
                });

                int fieldType = TEXTFIELD;
                if (property != null)
                {
                    List<Annotation> annotations = AnnotationUtils.annotationsFromProperty(property, true);
                    if (ignoreWizard && CollectionUtils.contains(annotations, new InstanceOfPredicate<Annotation>(Wizard.Ignore.class)))
                    {
                        continue;
                    }

                    fieldType = determineFieldType(property, annotations);
                }

                fields.add(new FieldInfo(fieldName, fieldType));
            }
        }
        catch (IntrospectionException e)
        {
            LOG.severe(e);
            throw new RuntimeException(e);
        }
    }

    private int determineFieldType(PropertyDescriptor property, List<Annotation> annotations)
    {
        int fieldType = TEXTFIELD;

        Field field = (Field) CollectionUtils.find(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                return annotation instanceof Field;
            }
        });

        Class<?> returnType = property.getReadMethod().getReturnType();
        boolean collection = List.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType);
        if (field == null)
        {
            if (returnType == Boolean.class || returnType == Boolean.TYPE)
            {
                fieldType = CHECKBOX;
            }
            else if (returnType.isEnum())
            {
                fieldType = COMBOBOX;
            }
            else if (List.class.isAssignableFrom(returnType))
            {
                fieldType = MULTI_SELECT;
            }
        }
        else
        {
            fieldType = convertFieldType(field.type(), collection);
        }

        return fieldType;
    }

    private int convertFieldType(String name, boolean collection)
    {
        if(name.equals(FieldType.CHECKBOX) || name.equals(FieldType.CONTROLLING_CHECKBOX))
        {
            return CHECKBOX;
        }
        else if(name.equals(FieldType.ITEM_PICKER))
        {
            return ITEM_PICKER;
        }
        else if(name.equals(FieldType.SELECT) || name.equals(FieldType.CONTROLLING_SELECT))
        {
            return collection ? COMBOBOX : MULTI_SELECT;
        }

        return TEXTFIELD;
    }

    /**
     * Indicates if a field is marked as inherited.
     *
     * @param fieldName name of the field to check
     * @return true iff the given field is marked inherited
     */
    public boolean isInherited(String fieldName)
    {
        return isAnnotationPresent(fieldName, ANNOTATION_INHERITED);
    }

    private static class FieldInfo
    {
        String name;
        int type;

        private FieldInfo(String name, int type)
        {
            this.name = name;
            this.type = type;
        }
    }
}
