package com.zutubi.prototype;

import com.zutubi.config.annotations.FieldType;
import com.zutubi.config.annotations.Handler;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.handler.AnnotationHandler;
import com.zutubi.prototype.handler.OptionAnnotationHandler;
import com.zutubi.prototype.model.*;
import com.zutubi.prototype.type.*;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.prototype.config.EnumOptionProvider;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.validators.RequiredValidator;

import java.lang.annotation.Annotation;
import java.util.*;

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
    private ObjectFactory objectFactory = new DefaultObjectFactory();

    private static final Logger LOG = Logger.getLogger(FormDescriptorFactory.class);

    // FIXME: extract these mappings to make them extendable.
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

    private Map<String, Class<? extends FieldDescriptor>> fieldDescriptorTypes = new HashMap<String, Class<? extends FieldDescriptor>>();
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private TypeRegistry typeRegistry;

    
    public void init()
    {
        // FIXME: incomplete
        registerFieldType(FieldType.CHECKBOX, CheckboxFieldDescriptor.class);
        registerFieldType(FieldType.CONTROLLING_CHECKBOX, ControllingCheckboxFieldDescriptor.class);
        registerFieldType(FieldType.HIDDEN, HiddenFieldDescriptor.class);
        registerFieldType(FieldType.PASSWORD, PasswordFieldDescriptor.class);
        registerFieldType(FieldType.SELECT, SelectFieldDescriptor.class);
        registerFieldType(FieldType.TEXT, TextFieldDescriptor.class);
        registerFieldType(FieldType.TEXTAREA, TextAreaFieldDescriptor.class);
    }

    public void registerFieldType(String type, Class<? extends FieldDescriptor> clazz)
    {
        fieldDescriptorTypes.put(type, clazz);
    }
    
    public FormDescriptor createDescriptor(String path, Class clazz)
    {
        return createDescriptor(path, typeRegistry.getType(clazz), "form");
    }

    public FormDescriptor createDescriptor(String path, String symbolicName)
    {
        return createDescriptor(path, typeRegistry.getType(symbolicName), "form");
    }

    public FormDescriptor createDescriptor(String path, CompositeType type, String name)
    {
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setName(name);
        descriptor.setActions(Arrays.asList("save", "cancel"));

        // The symbolic name uniquely identifies the type, and so will uniquely identify this form.
        // (we are not planning to have multiple forms on a single page at this stage...)
        descriptor.setId(type.getClazz().getName());

        // Process the annotations at apply to the type / form.
        List<Annotation> annotations = type.getAnnotations();
        handleAnnotations(type, descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(path, type, descriptor));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(String path, CompositeType type, FormDescriptor form)
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

        for (TypeProperty property : type.getProperties(SimpleType.class))
        {
            FieldDescriptor fd = createField(path, property, form);
            addFieldParameters(type, path, property, fd);
            fieldDescriptors.add(fd);
        }

        for(TypeProperty property: type.getProperties(CollectionType.class))
        {
            CollectionType propertyType = (CollectionType) property.getType();
            Type targetType = propertyType.getCollectionType();
            if(targetType instanceof EnumType || targetType instanceof ReferenceType)
            {
                SelectFieldDescriptor fd = new SelectFieldDescriptor();
                fd.setForm(form);
                fd.setPath(PathUtils.getPath(path, property.getName()));
                fd.setProperty(property);
                fd.setName(property.getName());
                fd.setMultiple(true);
                addFieldParameters(type, path, property, fd);
                fieldDescriptors.add(fd);
            }
        }

        return fieldDescriptors;
    }

    private FieldDescriptor createField(String path, TypeProperty property, FormDescriptor form)
    {
        String fieldType = FieldType.TEXT;
        com.zutubi.config.annotations.Field field = AnnotationUtils.findAnnotation(property.getAnnotations(), com.zutubi.config.annotations.Field.class);
        if(field != null)
        {
            fieldType = field.type();
        }
        else
        {
            SimpleType propertyType = (SimpleType) property.getType();
            if(propertyType instanceof PrimitiveType)
            {
                // some little bit of magic, take a guess at any property called password. If we come up with any
                // other magical cases, then we can refactor this a bit.
                if (property.getName().equals("password"))
                {
                    fieldType = "password";
                }
                else
                {
                    fieldType = defaultFieldTypeMapping.get(propertyType.getClazz());
                }
            }
            else if (propertyType instanceof EnumType)
            {
                fieldType = "select";
            }
        }

        FieldDescriptor fd = createFieldOfType(fieldType);
        fd.setType(fieldType);
        fd.setPath(PathUtils.getPath(path, property.getName()));
        fd.setProperty(property);
        fd.setName(property.getName());
        fd.setForm(form);
        return fd;
    }

    private FieldDescriptor createFieldOfType(String type)
    {
        Class<? extends FieldDescriptor> clazz = fieldDescriptorTypes.get(type);
        if(clazz == null)
        {
            return new FieldDescriptor();
        }
        else
        {
            try
            {
                return clazz.newInstance();
            }
            catch (Exception e)
            {
                LOG.severe(e);
                return new FieldDescriptor();
            }
        }
    }

    private void addFieldParameters(CompositeType type, String path, TypeProperty property, FieldDescriptor fd)
    {
        handleAnnotations(type, fd, property.getAnnotations());
        if(fd instanceof SelectFieldDescriptor)
        {
            SelectFieldDescriptor select = (SelectFieldDescriptor) fd;
            if (select.getList() == null)
            {
                addDefaultOptions(path, property, select);
            }
        }
    }

    private void addDefaultOptions(String path, TypeProperty typeProperty, SelectFieldDescriptor fd)
    {
        if(typeProperty.getType().getTargetType() instanceof EnumType)
        {
            OptionAnnotationHandler.process(configurationPersistenceManager, new EnumOptionProvider(), path, fd);
        }
        else
        {
            fd.setList(Collections.EMPTY_LIST);
        }
    }

    /**
     * This handle annotation method will serach through the annotaion
     * hierarchy, looking for annotations that have a handler mapped to them.
     * When found, the handler is run in the context of the annotation and
     * the descriptor.
     *
     * Note: This method will process all of the annotation's annotations as well.
     *
     * @param type       the composite type that has been annotated (or meta-annotated)
     * @param descriptor the target that will be modified by these annotations.
     * @param annotations the annotations that need to be processed.
     */
    private void handleAnnotations(CompositeType type, Descriptor descriptor, List<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getName().startsWith("java.lang"))
            {
                // ignore standard annotations.
                continue;
            }

            // recurse up the annotation hierarchy.
            handleAnnotations(type, descriptor, Arrays.asList(annotationType.getAnnotations()));
            
            Handler handlerAnnotation = annotationType.getAnnotation(Handler.class);
            if (handlerAnnotation != null)
            {
                try
                {
                    AnnotationHandler handler = objectFactory.buildBean(handlerAnnotation.className());
                    handler.process(type, annotation, descriptor);
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
            // This needs to be handled by a handler somehow - remembering that the annotations themselves
            // are not part of the form package.  ...
            if (annotation instanceof Constraint)
            {
                FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
                fieldDescriptor.setConstrained(true);
                List<String> constraints = Arrays.asList(((Constraint)annotation).value());
                if (constraints.contains(RequiredValidator.class.getName()))
                {
                    fieldDescriptor.setRequired(true);
                }
            }
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
