package com.zutubi.prototype.form;

import com.zutubi.prototype.form.annotation.Table;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.prototype.RecordTypeInfo;
import com.zutubi.pulse.prototype.RecordPropertyInfo;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

/**
 *
 *
 */
public class TableDescriptorFactory
{
    private RecordTypeRegistry typeRegistry;

    public List<TableDescriptor> createDescriptors(Class type) throws IntrospectionException
    {
        List<TableDescriptor> tableDescriptors = new LinkedList<TableDescriptor>();

        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);

        RecordTypeInfo typeInfo = typeRegistry.getInfo(type);

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
        {
            List<Annotation> annotations = AnnotationUtils.annotationsFromField(propertyDescriptor);
            Annotation tableAnnotation = CollectionUtils.find(annotations, new Predicate<Annotation>()
            {
                public boolean satisfied(Annotation annotation)
                {
                    return annotation instanceof Table;
                }
            });
            
            if (tableAnnotation != null)
            {
                tableDescriptors.add(createTableDescriptor(propertyDescriptor, tableAnnotation, typeInfo));
            }
        }
        return tableDescriptors;
    }

    public TableDescriptor createTableDescriptor(PropertyDescriptor propertyDescriptor, Annotation tableAnnotation, RecordTypeInfo typeInfo)
    {
        TableDescriptor tableDescriptor = new TableDescriptor();
        tableDescriptor.setName(propertyDescriptor.getName());

        // generate columns...

        RecordPropertyInfo propertyInfo = typeInfo.getProperty(propertyDescriptor.getName());
        propertyInfo.getType();



        return tableDescriptor;
    }

    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
