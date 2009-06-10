package com.zutubi.pulse.acceptance.forms;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.tove.annotations.Field;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.AnnotationUtils;
import com.zutubi.pulse.acceptance.SeleniumBrowser;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Abstract base for forms that are based off a configuration class, where
 * the class is available (i.e. not a plugin).  The form and field names are
 * automatically determined from the class.
 */
public class ConfigurationForm extends SeleniumForm
{
    public static final String ANNOTATION_INHERITED = "inherited";

    private static final Logger LOG = Logger.getLogger(ConfigurationForm.class);
    private Class<? extends Configuration> configurationClass;

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass)
    {
        super(browser);
        this.configurationClass = configurationClass;
    }

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass, boolean ajax)
    {
        super(browser, ajax);
        this.configurationClass = configurationClass;
    }

    public ConfigurationForm(SeleniumBrowser browser, Class<? extends Configuration> configurationClass, boolean ajax, boolean inherited)
    {
        super(browser, ajax, inherited);
        this.configurationClass = configurationClass;
    }

    public String getFormName()
    {
        return configurationClass.getName();
    }

    public String[] getFieldNames()
    {
        return configurationClass.getAnnotation(Form.class).fieldOrder();
    }

    public int[] getFieldTypes()
    {
        final String[] fieldNames = getFieldNames();
        int[] fieldTypes = new int[fieldNames.length];

        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(configurationClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for(int i = 0; i < fieldNames.length; i++)
            {
                final String fieldName = fieldNames[i];
                fieldTypes[i] = TEXTFIELD;
                PropertyDescriptor property = CollectionUtils.find(propertyDescriptors, new Predicate<PropertyDescriptor>()
                {
                    public boolean satisfied(PropertyDescriptor propertyDescriptor)
                    {
                        return propertyDescriptor.getName().equals(fieldName);
                    }
                });

                if(property != null)
                {
                    List<Annotation> annotations = AnnotationUtils.annotationsFromProperty(property, true);
                    Field field = (Field) CollectionUtils.find(annotations, new Predicate<Annotation>()
                    {
                        public boolean satisfied(Annotation annotation)
                        {
                            return annotation instanceof Field;
                        }
                    });

                    Class<?> returnType = property.getReadMethod().getReturnType();
                    boolean collection = List.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType);
                    if(field == null)
                    {
                        if(returnType == Boolean.class || returnType == Boolean.TYPE)
                        {
                            fieldTypes[i] = CHECKBOX;
                        }
                        else if(returnType.isEnum())
                        {
                            fieldTypes[i] = COMBOBOX;
                        }
                        else if(List.class.isAssignableFrom(returnType))
                        {
                            fieldTypes[i] = MULTI_SELECT;
                        }
                    }
                    else
                    {
                        fieldTypes[i] = convertFieldType(field.type(), collection);
                    }
                }
            }
        }
        catch (IntrospectionException e)
        {
            LOG.severe(e);
            return super.getFieldTypes();
        }

        return fieldTypes;
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
}
