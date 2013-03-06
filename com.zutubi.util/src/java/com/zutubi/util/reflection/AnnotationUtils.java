package com.zutubi.util.reflection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.util.bean.BeanUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static java.util.Arrays.asList;

/**
 *
 */
public class AnnotationUtils
{
    private static final Package ANNOTATION_PACKAGE = Package.getPackage("java.lang.annotation");

    /**
     * Finds all annotations attached to the give property.  Annotations can
     * be found on the read and write methods as well as the field
     * declaration if one exists.  The inheritance hierarchy is also
     * searched, so if property appears in the superclass, annotations in the
     * superclass are also included.  Finally, if includeMeta is true,
     * annotations on the annotations found will also be included,
     * transitively.
     *
     * @param property    the property to retrieve annotations for
     * @param includeMeta if true, meta-annotations (annotations on
     *                    annotations) are also included
     * @return the annotations for the given property
     * @throws IntrospectionException if reflection fails
     */
    public static List<Annotation> annotationsFromProperty(final PropertyDescriptor property, boolean includeMeta) throws IntrospectionException
    {
        List<Annotation> annotations = new LinkedList<Annotation>();

        Class declaringClass = null;

        Method readMethod = property.getReadMethod();
        if (readMethod != null)
        {
            annotations.addAll(asList(readMethod.getAnnotations()));
            declaringClass = readMethod.getDeclaringClass();
        }

        Method writeMethod = property.getWriteMethod();
        if (writeMethod != null)
        {
            annotations.addAll(asList(writeMethod.getAnnotations()));
            declaringClass = writeMethod.getDeclaringClass();
        }

        if (declaringClass != null)
        {
            try
            {
                Field field = declaringClass.getDeclaredField(property.getName());
                annotations.addAll(asList(field.getAnnotations()));
            }
            catch (NoSuchFieldException e)
            {
                // noop.
            }

            // Look for the meta annotations now.  Meta annotations for
            // superclass properties are found by the mutual recursion into
            // this method.
            if (includeMeta)
            {
                annotations = addMetaAnnotations(annotations);
            }

            Class superClass = declaringClass.getSuperclass();
            if (superClass != null && superClass != Object.class)
            {
                processSuper(superClass, property, annotations, includeMeta);
            }

            for (Class superInterface : declaringClass.getInterfaces())
            {
                processSuper(superInterface, property, annotations, includeMeta);
            }
        }

        return annotations;
    }

    private static List<Annotation> addMetaAnnotations(List<Annotation> annotations)
    {
        Set<Class<? extends Annotation>> seenTypes = new HashSet<Class<? extends Annotation>>();
        seenTypes.addAll(transform(annotations, new Function<Annotation, Class<? extends Annotation>>()
        {
            public Class<? extends Annotation> apply(Annotation annotation)
            {
                return annotation.annotationType();
            }
        }));

        List<Annotation> result = new LinkedList<Annotation>();
        Queue<Annotation> toProcess = new LinkedList<Annotation>(annotations);
        while (!toProcess.isEmpty())
        {
            Annotation a = toProcess.remove();
            result.add(a);
            
            Class<? extends Annotation> type = a.annotationType();
            seenTypes.add(type);
            for (Annotation meta : type.getAnnotations())
            {
                if (isUserDeclared(meta.annotationType()) && !seenTypes.contains(meta.annotationType()))
                {
                    toProcess.offer(meta);
                }
            }
        }

        return result;
    }

    private static boolean isUserDeclared(Class<? extends Annotation> annotationType)
    {
        return annotationType.getPackage() != ANNOTATION_PACKAGE;
    }

    private static void processSuper(Class superClass, final PropertyDescriptor property, List<Annotation> annotations, boolean includeMeta) throws IntrospectionException
    {
        BeanInfo superInfo = Introspector.getBeanInfo(superClass);
        PropertyDescriptor superDescriptor = find(asList(superInfo.getPropertyDescriptors()), new Predicate<PropertyDescriptor>()
        {
            public boolean apply(PropertyDescriptor superProperty)
            {
                return superProperty.getName().equals(property.getName());
            }
        }, null);

        if (superDescriptor != null)
        {
            annotations.addAll(annotationsFromProperty(superDescriptor, includeMeta));
        }
    }

    /**
     * This method will check if an annotation property has a default value by checking
     * if there is a field named DEFAULT_ + property name.
     *
     * @param annotation       annotation instance to check
     * @param annotationMethod getter method for the property value
     * @return true if the property has the default value
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
     * @param annotation annotation instance to grab properties from
     * @param target     object on which to set the property values
     */
    public static void setPropertiesFromAnnotation(Annotation annotation, Object target)
    {
        for (Method annotationMethod : annotation.getClass().getMethods())
        {
            try
            {
                if (isUserDeclared(annotationMethod) && !isDefault(annotation, annotationMethod))
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
        List<Annotation> from = annotationsFromProperty(property, false);
        return findAnnotation(from, clazz);
    }

    public static <T extends Annotation> T findAnnotation(List<Annotation> from, Class<T> clazz)
    {
        Set<Class<? extends Annotation>> seenTypes = new HashSet<Class<? extends Annotation>>();
        seenTypes.addAll(transform(from, new Function<Annotation, Class<? extends Annotation>>()
        {
            public Class<? extends Annotation> apply(Annotation annotation)
            {
                return annotation.annotationType();
            }
        }));

        Queue<Annotation> toProcess = new LinkedList<Annotation>(from);
        while (!toProcess.isEmpty())
        {
            Annotation a = toProcess.remove();
            if (clazz.isInstance(a))
            {
                return clazz.cast(a);
            }

            Class<? extends Annotation> type = a.annotationType();
            seenTypes.add(type);
            for (Annotation meta : type.getAnnotations())
            {
                if (!seenTypes.contains(meta.annotationType()))
                {
                    toProcess.offer(meta);
                }
            }
        }

        return null;
    }

    /**
     * Returns the property ot the given class (or superclass) that is
     * annotated with an annotation of the given type.
     *
     * @param clazz          type to check the properties of
     * @param annotationType type of annotation to check for
     * @return the annotated property, or null of none is found
     * @throws IntrospectionException on reflection error
     */
    public static <T extends Annotation> String getPropertyAnnotatedWith(Class clazz, Class<T> annotationType) throws IntrospectionException
    {
        for (PropertyDescriptor descriptor : ReflectionUtils.getBeanProperties(clazz))
        {
            List<Annotation> annotations = AnnotationUtils.annotationsFromProperty(descriptor, false);
            for (Annotation a : annotations)
            {
                if (a.annotationType() == annotationType)
                {
                    return descriptor.getName();
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
