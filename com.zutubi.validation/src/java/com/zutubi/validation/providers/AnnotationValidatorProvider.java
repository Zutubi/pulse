package com.zutubi.validation.providers;

import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.reflection.AnnotationUtils;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.ConstraintProperty;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Provides validators based on the type of an object.  The object's class
 * (and its superclasses) are analysed for {@link com.zutubi.validation.annotations.Constraint}
 * references, applied either directly to properties or indirectly through
 * other annotations on properties.
 */
public class AnnotationValidatorProvider implements ValidatorProvider
{
    // unless an object factory is specified, use the default. We may not always be in a
    // happy autowiring context.
    private ObjectFactory objectFactory = new DefaultObjectFactory();
    /**
     * High-level cache of the results of {@link #getValidators(Class, com.zutubi.validation.ValidationContext)}.
     */
    private Map<Class, List<Validator>> secondLevelCache = Collections.synchronizedMap(new HashMap<Class, List<Validator>>());
    /**
     * Low-level cache of the result of {@link #analyze(Class)}.
     */
    private Map<Class, List<Validator>> firstLevelCache = Collections.synchronizedMap(new HashMap<Class, List<Validator>>());

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    @Override
    public List<Validator> getValidators(Class clazz, ValidationContext context)
    {
        List<Validator> validators = secondLevelCache.get(clazz);
        if (validators == null)
        {
            validators = traverse(clazz, new HashSet<Class>());
            secondLevelCache.put(clazz, validators);
        }

        return validators;
    }

    public List<Validator> traverse(Class clazz, Set<Class> checked)
    {
        List<Validator> validators = new LinkedList<>();

        if (checked.contains(clazz) || clazz.equals(Object.class))
        {
            return validators;
        }

        // traverse our way up the hierarchy.
        if (clazz.isInterface())
        {
            for (Class interfaceClass : clazz.getInterfaces())
            {
                validators.addAll(traverse(interfaceClass, checked));
            }
        }
        else
        {
            validators.addAll(traverse(clazz.getSuperclass(), checked));
        }

        // traverse across the hierarchy
        for (Class interfaceClass : clazz.getInterfaces())
        {
            if (!checked.contains(interfaceClass))
            {
                validators.addAll(traverse(interfaceClass, checked));
            }
        }

        // analyse the class
        if (!checked.contains(clazz))
        {
            validators.addAll(analyze(clazz));
            checked.add(clazz);
        }

        return validators;
    }

    private List<Validator> analyze(Class clazz)
    {
        // validators are based on the runtime type of the object, not the runtime state, so we can happily cache
        // the details.
        List<Validator> validators = firstLevelCache.get(clazz);
        if(validators == null)
        {
            validators = new LinkedList<>();
            try
            {
                Class stopClass = clazz.getSuperclass();
                for (PropertyDescriptor descriptor : Introspector.getBeanInfo(clazz, stopClass).getPropertyDescriptors())
                {
                    // the property must be readable for us to be able to validate it.
                    Method read = descriptor.getReadMethod();
                    if (read != null)
                    {
                        List<Annotation> constraints = new LinkedList<>();
                        constraints.addAll(constraintsOn(read));
                        Method write = descriptor.getWriteMethod();
                        if (write != null)
                        {
                            constraints.addAll(constraintsOn(write));
                        }

                        // analyse the field (if it exists).
                        try
                        {
                            Field field = clazz.getDeclaredField(descriptor.getName());
                            constraints.addAll(constraintsOn(field));
                        }
                        catch (NoSuchFieldException e)
                        {
                            // noop
                        }

                        // convert constraints into validators.
                        validators.addAll(validatorsFromConstraints(clazz, constraints, descriptor));
                    }
                }

                firstLevelCache.put(clazz, validators);
            }
            catch (IntrospectionException e)
            {
                // noop.
            }
        }

        return validators;
    }

    private Collection<Annotation> constraintsOn(AnnotatedElement element)
    {
        if (element == null)
        {
            return Collections.emptySet();
        }
        
        List<Annotation> constraints = new LinkedList<>();
        for (Annotation annotation : element.getAnnotations())
        {
            if (isConstraint(annotation))
            {
                constraints.add(annotation);
            }
        }
        return constraints;
    }

    private boolean isConstraint(Annotation annotation)
    {
        return annotation instanceof Constraint || annotation.annotationType().getAnnotation(Constraint.class) != null;
    }

    private List<Validator> validatorsFromConstraints(Class clazz, List<Annotation> constraints, PropertyDescriptor descriptor)
    {
        List<Validator> validators = new LinkedList<>();
        for (Annotation annotation : constraints)
        {
            Constraint constraint;
            if(annotation instanceof Constraint)
            {
                constraint = (Constraint) annotation;
            }
            else
            {
                constraint = annotation.annotationType().getAnnotation(Constraint.class);
            }
            
            for (String validatorClassName : constraint.value())
            {
                try
                {
                    Class<? extends Validator> validatorClass = ClassLoaderUtils.loadAssociatedClass(clazz, validatorClassName);
                    Validator validator = objectFactory.buildBean(validatorClass);
                    copyConstraintProperties(annotation, validator);
                    if (validator instanceof FieldValidator)
                    {
                        ((FieldValidator)validator).setFieldName(descriptor.getName());
                    }
                    validators.add(validator);
                }
                catch (Exception e)
                {
                    // noop.
                    e.printStackTrace();
                }
            }
        }
        return validators;
    }

    private void copyConstraintProperties(Annotation annotation, Validator validator)
    {
        for (Method annotationMethod : annotation.annotationType().getMethods())
        {
            try
            {
                if (AnnotationUtils.isUserDeclared(annotationMethod) && !AnnotationUtils.isDefault(annotation, annotationMethod))
                {
                    String targetProperty;
                    ConstraintProperty declaredTargetProperty = annotationMethod.getAnnotation(ConstraintProperty.class);
                    if (declaredTargetProperty == null)
                    {
                        targetProperty = annotationMethod.getName();
                    }
                    else
                    {
                        targetProperty = declaredTargetProperty.value();
                    }

                    BeanUtils.setProperty(targetProperty, annotationMethod.invoke(annotation), validator);
                }
            }
            catch (Exception e)
            {
                // noop.
            }
        }
    }
}
