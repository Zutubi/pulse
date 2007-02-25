package com.zutubi.prototype;

import com.zutubi.prototype.annotation.AnnotationHandler;
import com.zutubi.prototype.annotation.Handler;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class FormDescriptorFactory
{
    /**
     * The object factory is required for the instantiation of objects that occurs within the form descriptor.
     * To ensure that this always works, we default to a base implementation of the Object factory, which simply
     * instantiated objects.  When deployed, this should be replaced by the auto wiring object factory.
     */
    private ObjectFactory objectFactory = new ObjectFactory();

    private static final Logger LOG = Logger.getLogger(FormDescriptorFactory.class);

    // TODO: extract this field type mapping to make it extendable.

    private static final Map<Class, String> defaultFieldTypeMapping = new HashMap<Class, String>();
    static
    {
        defaultFieldTypeMapping.put(String.class, "text");
        defaultFieldTypeMapping.put(Boolean.class, "checkbox");
        defaultFieldTypeMapping.put(Boolean.TYPE, "checkbox");
        defaultFieldTypeMapping.put(Integer.class, "text");
        defaultFieldTypeMapping.put(Integer.TYPE, "text");
        defaultFieldTypeMapping.put(Long.class, "text");
        defaultFieldTypeMapping.put(Long.TYPE, "text");
    }

    private TypeRegistry typeRegistry;

    public FormDescriptor createDescriptor(Class clazz)
    {
        Type type = typeRegistry.getType(clazz);
        return createDescriptor(type);
    }

    public FormDescriptor createDescriptor(String symbolicName)
    {
        Type type = typeRegistry.getType(symbolicName);
        return createDescriptor(type);
    }

    public FormDescriptor createDescriptor(Type type)
    {
        FormDescriptor descriptor = new FormDescriptor();

        // The symbolic name uniquely identifies the type, and so will uniquely identify this form.
        // (we are not planning to have multiple forms on a single page at this stage...) 
        descriptor.setId(type.getSymbolicName());

        // Process the annotations at apply to the type / form.
        List<Annotation> annotations = type.getAnnotations();
        handleAnnotations(descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(type));
        descriptor.setActions(Arrays.asList("save", "cancel"));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(Type type)
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

        for (TypeProperty property : type.getProperties(PrimitiveType.class))
        {
            Type propertyType = property.getType();
            FieldDescriptor fd = new FieldDescriptor();
            fd.setName(property.getName());

            // some little bit of magic, take a guess at any property called password. If we come up with any
            // other magical cases, then we can refactor this a bit.
            if (fd.getName().equals("password"))
            {
                fd.setType("password");
                fd.addParameter("type", "password");
            }
            else
            {
                fd.addParameter("type", defaultFieldTypeMapping.get(propertyType.getClazz()));
            }

            handleAnnotations(fd, property.getAnnotations());

            fieldDescriptors.add(fd);
        }

        return fieldDescriptors;
    }

    /**
     * This handle annotation method will serach through the annotaion hierarchy, looking for annotations that
     * are themselves annotated by the Handler annotation.  When found, the referenced handler is run in the context
     * of the annotation and the descriptor.
     *
     * Note: This method will process all of the annotations annotations as well.
     *
     * @param descriptor the target that will be modified by these annotations.
     * @param annotations the annotations that need to be processed. 
     */
    private void handleAnnotations(Descriptor descriptor, List<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().getName().startsWith("java.lang"))
            {
                // ignore standard annotations.
                continue;
            }

            // recurse up the annotation hierarchy.
            handleAnnotations(descriptor, Arrays.asList(annotation.annotationType().getAnnotations()));

            if (annotation.annotationType().isAnnotationPresent(Handler.class))
            {
                Handler handlerAnnotation = annotation.annotationType().getAnnotation(Handler.class);
                try
                {
                    AnnotationHandler handler = objectFactory.buildBean(handlerAnnotation.value());
                    handler.process(annotation, descriptor);
                }
                catch (InstantiationException e)
                {
                    LOG.warning("Failed to instantiate annotation handler.", e); // failed to instantiate the annotation handler.
                }
                catch (IllegalAccessException e)
                {
                    LOG.warning("Failed to instantiate annotation handler.", e); // failed to instantiate the annotation handler.
                }
                catch (Exception e)
                {
                    LOG.warning("Unexpected exception processing the annotation handler.", e);
                }
            }
        }
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance.
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
