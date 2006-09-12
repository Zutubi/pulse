package com.zutubi.validation.providers;

import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.Validator;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.bean.ObjectFactory;
import com.zutubi.validation.bean.AnnotationUtils;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * <class-comment/>
 */
public class AnnotationValidatorProvider implements ValidatorProvider
{
    private ObjectFactory objectFactory;

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public List<Validator> getValidators(Object obj)
    {
        // validators are based on the runtime type of the object, not the runtime state, so we can happily cache
        // the details.

        List<Validator> validators = new LinkedList<Validator>();
        try
        {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(obj.getClass(), Object.class).getPropertyDescriptors())
            {
                List<Annotation> constraints = new LinkedList<Annotation>();
                constraints.addAll(constraintsOnMethod(descriptor.getReadMethod()));
                constraints.addAll(constraintsOnMethod(descriptor.getWriteMethod()));

                // convert constraints into validators.
                validators.addAll(validatorsFromConstraints(constraints, descriptor));
            }
        }
        catch (IntrospectionException e)
        {
            // noop.
        }
        return validators;
    }

    private Collection<Annotation> constraintsOnMethod(Method method)
    {
        if (method == null)
        {
            return Collections.EMPTY_SET;
        }
        List<Annotation> constraints = new LinkedList<Annotation>();
        for (Annotation annotation : method.getAnnotations())
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
        return annotation.annotationType().getAnnotation(Constraint.class) != null;
    }

    private List<Validator> validatorsFromConstraints(List<Annotation> constraints, PropertyDescriptor descriptor)
    {
        List<Validator> validators = new LinkedList<Validator>();
        for (Annotation annotation : constraints)
        {
            Constraint constraint = annotation.annotationType().getAnnotation(Constraint.class);
            try
            {
                Validator validator = objectFactory.buildBean(constraint.handler());
                AnnotationUtils.setPropertiesFromAnnotation(annotation, validator);
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
        return validators;
    }
}
