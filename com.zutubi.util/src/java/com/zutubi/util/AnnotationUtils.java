package com.zutubi.util;

import com.zutubi.util.bean.BeanUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 
 */
public class AnnotationUtils
{
    public static List<Annotation> annotationsFromProperty(final PropertyDescriptor property) throws IntrospectionException
    {
        List<Annotation> annotations = new LinkedList<Annotation>();

        Class declaringClass = null;

        Method readMethod = property.getReadMethod();
        if (readMethod != null)
        {
            annotations.addAll(Arrays.asList(readMethod.getAnnotations()));
            declaringClass = readMethod.getDeclaringClass();
        }

        Method writeMethod = property.getWriteMethod();
        if (writeMethod != null)
        {
            annotations.addAll(Arrays.asList(writeMethod.getAnnotations()));
            declaringClass = writeMethod.getDeclaringClass();
        }

        if (declaringClass != null)
        {
            try
            {
                Field field = declaringClass.getDeclaredField(property.getName());
                annotations.addAll(Arrays.asList(field.getAnnotations()));
            }
            catch (NoSuchFieldException e)
            {
                // noop.
            }

            Class superClass = declaringClass.getSuperclass();
            if(superClass != null && superClass != Object.class)
            {
                processSuper(superClass, property, annotations);
            }

            for(Class superInterface: declaringClass.getInterfaces())
            {
                processSuper(superInterface, property, annotations);
            }
        }

        return annotations;
    }

    private static void processSuper(Class superClass, final PropertyDescriptor property, List<Annotation> annotations) throws IntrospectionException
    {
        BeanInfo superInfo = Introspector.getBeanInfo(superClass);
        PropertyDescriptor superDescriptor = CollectionUtils.find(superInfo.getPropertyDescriptors(), new Predicate<PropertyDescriptor>()
        {
            public boolean satisfied(PropertyDescriptor superProperty)
            {
                return superProperty.getName().equals(property.getName());
            }
        });

        if(superDescriptor != null)
        {
            annotations.addAll(annotationsFromProperty(superDescriptor));
        }
    }

    /**
     * This method will check if an annotation property has a default value by checking
     * if there is a field named DEFAULT_ + property name.
     *
     * @param annotation
     * @param annotationMethod
     *
     */
    public static boolean isDefault(Annotation annotation, Method annotationMethod)
    {
        try
        {
            java.lang.reflect.Field defaultField = annotation.getClass().getField("DEFAULT_" + annotationMethod.getName());
            if (defaultField != null)
            {
                if (annotationMethod.invoke(annotation).equals(
                        defaultField.get(annotation)))
                {
                    return true;
                }
            }
            return false;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * For each attribute of annotation, will search for a matching property on
     * the target and set it with the value of the attribute unless the attribute
     * is set to the "default" value
     *
     * @param annotation
     *
     * @param target
     */
    public static void setPropertiesFromAnnotation(Annotation annotation, Object target)
    {
        for (Method annotationMethod : annotation.getClass().getMethods())
        {
            try
            {
                if (!isDefault(annotation, annotationMethod))
                {
                    BeanUtils.setProperty(
                            annotationMethod.getName(),
                            annotationMethod.invoke(annotation),
                            target
                    );
                }

            }
            catch (Exception e)
            {
                // noop.
            }
        }
    }

    public static Map<String, Object> collectPropertiesFromAnnotation(Annotation annotation)
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Method annotationMethod : annotation.getClass().getDeclaredMethods())
        {
            try
            {
                if (isUserDeclared(annotationMethod) && !isDefault(annotation, annotationMethod))
                {
                    properties.put(annotationMethod.getName(), annotationMethod.invoke(annotation));
                }
            }
            catch (Exception e)
            {
                // noop.
            }
        }
        return properties;
    }

    public static <T extends Annotation> T findAnnotation(PropertyDescriptor property, Class<T> clazz) throws IntrospectionException
    {
        List<Annotation> from = annotationsFromProperty(property);
        return findAnnotation(from, clazz);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends Annotation> T findAnnotation(List<Annotation> from, Class<T> clazz)
    {
        Set<Class<? extends Annotation>> seenTypes = new HashSet<Class<? extends Annotation>>();
        CollectionUtils.map(from, new Mapping<Annotation, Class<? extends Annotation>>()
        {
            public Class<? extends Annotation> map(Annotation annotation)
            {
                return annotation.annotationType();
            }
        }, seenTypes);

        Queue<Annotation> toProcess = new LinkedList<Annotation>(from);
        while(!toProcess.isEmpty())
        {
            Annotation a = toProcess.remove();
            if(clazz.isInstance(a))
            {
                return (T) a;
            }

            Class<? extends Annotation> type = a.annotationType();
            seenTypes.add(type);
            for(Annotation meta: type.getAnnotations())
            {
                if(!seenTypes.contains(meta.annotationType()))
                {
                    toProcess.offer(meta);
                }
            }
        }

        return null;
    }

    private static final Set<String> internalMethods = new HashSet<String>();
    static
    {
        internalMethods.add("toString");
        internalMethods.add("hashCode");
        internalMethods.add("annotationType");
        internalMethods.add("equals");
    }

    public static boolean isUserDeclared(Method annotationMethod)
    {
        return !internalMethods.contains(annotationMethod.getName());
    }
}
