package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.config.EnumOptionProvider;
import com.zutubi.pulse.master.tove.handler.AnnotationHandler;
import com.zutubi.pulse.master.tove.handler.OptionAnnotationHandler;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.annotations.Handler;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.config.ConfigurationValidatorProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.AnnotationUtils;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.Validator;
import com.zutubi.validation.annotations.Numeric;
import com.zutubi.validation.validators.RequiredValidator;

import java.io.File;
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

    private static final Map<Class, String> defaultFieldTypeMapping = new HashMap<Class, String>();

    static
    {
        defaultFieldTypeMapping.put(String.class, "text");
        defaultFieldTypeMapping.put(File.class, "text");
        defaultFieldTypeMapping.put(Boolean.class, "checkbox");
        defaultFieldTypeMapping.put(Boolean.TYPE, "checkbox");
        defaultFieldTypeMapping.put(Integer.class, "text");
        defaultFieldTypeMapping.put(Integer.TYPE, "text");
        defaultFieldTypeMapping.put(Long.class, "text");
        defaultFieldTypeMapping.put(Long.TYPE, "text");
    }

    private Map<String, Class<? extends FieldDescriptor>> fieldDescriptorTypes = new HashMap<String, Class<? extends FieldDescriptor>>();
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationValidatorProvider configurationValidatorProvider;
    private ConfigurationProvider configurationProvider;

    public void init()
    {
        registerFieldType(FieldType.CHECKBOX, CheckboxFieldDescriptor.class);
        registerFieldType(FieldType.CONTROLLING_CHECKBOX, ControllingCheckboxFieldDescriptor.class);
        registerFieldType(FieldType.CONTROLLING_SELECT, ControllingSelectFieldDescriptor.class);
        registerFieldType(FieldType.HIDDEN, HiddenFieldDescriptor.class);
        registerFieldType(FieldType.PASSWORD, PasswordFieldDescriptor.class);
        registerFieldType(FieldType.SELECT, SelectFieldDescriptor.class);
        registerFieldType(FieldType.ITEM_PICKER, ItemPickerFieldDescriptor.class);
        registerFieldType(FieldType.TEXT, TextFieldDescriptor.class);
        registerFieldType(FieldType.TEXTAREA, TextAreaFieldDescriptor.class);
    }

    public void registerFieldType(String type, Class<? extends FieldDescriptor> clazz)
    {
        fieldDescriptorTypes.put(type, clazz);
    }

    public FormDescriptor createDescriptor(String parentPath, String baseName, CompositeType type, boolean concrete, String name)
    {
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setName(name);
        descriptor.setActions("save", "cancel");

        // The symbolic name uniquely identifies the type, and so will uniquely identify this form.
        // (we are not planning to have multiple forms on a single page at this stage...)
        descriptor.setId(type.getClazz().getName());

        // Process the annotations at apply to the type / form.
        List<Annotation> annotations = type.getAnnotations(true);

        // We accept inherited annotations, but only process the most locally-
        // declared of each type.
        final Set<Class> seenTypes = new HashSet<Class>();
        annotations = CollectionUtils.filter(annotations, new Predicate<Annotation>()
        {
            public boolean satisfied(Annotation annotation)
            {
                boolean satisfied = !seenTypes.contains(annotation.getClass());
                seenTypes.add(annotation.getClass());
                return satisfied;
            }
        });

        handleAnnotations(type, descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(parentPath, baseName, type, concrete, descriptor));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(String parentPath, String baseName, CompositeType type, boolean concrete, FormDescriptor form)
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();
        List<Validator> validators = getValidators(parentPath, baseName, concrete, type);

        for (TypeProperty property : type.getProperties(SimpleType.class))
        {
            FieldDescriptor fd = createField(parentPath, baseName, property, form);
            addFieldParameters(type, parentPath, baseName, property, fd, validators);
            fieldDescriptors.add(fd);
        }

        for (TypeProperty property : type.getProperties(CollectionType.class))
        {
            CollectionType propertyType = (CollectionType) property.getType();
            Type targetType = propertyType.getCollectionType();
            if (targetType instanceof SimpleType)
            {
                String fieldType = FieldType.SELECT;
                com.zutubi.tove.annotations.Field field = AnnotationUtils.findAnnotation(property.getAnnotations(), com.zutubi.tove.annotations.Field.class);
                if (field != null)
                {
                    fieldType = field.type();
                }

                FieldDescriptor fd = createFieldOfType(fieldType);
                if (fd instanceof OptionFieldDescriptor)
                {
                    ((OptionFieldDescriptor) fd).setMultiple(true);
                }

                fd.setForm(form);
                fd.setParentPath(parentPath);
                fd.setBaseName(baseName);
                fd.setProperty(property);
                fd.setName(property.getName());
                addFieldParameters(type, parentPath, baseName, property, fd, validators);
                fieldDescriptors.add(fd);
            }
        }

        return fieldDescriptors;
    }

    private List<Validator> getValidators(String parentPath, String baseName, boolean concrete, CompositeType type)
    {
        List<Validator> validators;
        try
        {
            Configuration dummyInstance = type.getClazz().newInstance();
            ConfigurationValidationContext validationContext = new ConfigurationValidationContext(dummyInstance, null, parentPath, baseName, !concrete, false, configurationTemplateManager);
            validators = configurationValidatorProvider.getValidators(dummyInstance, validationContext);
        }
        catch (Throwable e)
        {
            // Not ideal, but we can soldier on regardless.
            LOG.warning("Unable to get validators for type '" + type.getSymbolicName() + "': " + e.getMessage(), e);
            validators = new ArrayList<Validator>(0);
        }
        return validators;
    }

    private FieldDescriptor createField(String parentPath, String baseName, TypeProperty property, FormDescriptor form)
    {
        String fieldType = FieldType.TEXT;
        com.zutubi.tove.annotations.Field field = AnnotationUtils.findAnnotation(property.getAnnotations(), com.zutubi.tove.annotations.Field.class);
        if (field != null)
        {
            fieldType = field.type();
        }
        else
        {
            SimpleType propertyType = (SimpleType) property.getType();
            if (propertyType instanceof PrimitiveType)
            {
                // some little bit of magic, take a guess at any property called password. If we come up with any
                // other magical cases, then we can refactor this a bit.
                if (property.getName().equals("password"))
                {
                    fieldType = FieldType.PASSWORD;
                }
                else
                {
                    fieldType = defaultFieldTypeMapping.get(propertyType.getClazz());
                }
            }
            else if (propertyType instanceof EnumType)
            {
                fieldType = FieldType.SELECT;
            }
        }

        FieldDescriptor fd = createFieldOfType(fieldType);
        fd.setType(fieldType);
        fd.setParentPath(parentPath);
        fd.setBaseName(baseName);
        fd.setProperty(property);
        fd.setName(property.getName());
        fd.setForm(form);
        return fd;
    }

    private FieldDescriptor createFieldOfType(String type)
    {
        Class<? extends FieldDescriptor> clazz = fieldDescriptorTypes.get(type);
        if (clazz == null)
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

    private void addFieldParameters(CompositeType type, String parentPath, String baseName, TypeProperty property, FieldDescriptor fd, List<Validator> validators)
    {
        handleAnnotations(type, fd, property.getAnnotations());

        if (!property.isWriteable())
        {
            fd.addParameter("readOnly", true);
        }

        if (fd instanceof TextFieldDescriptor)
        {
            Numeric numeric = AnnotationUtils.findAnnotation(property.getAnnotations(), Numeric.class);
            if (numeric != null)
            {
                fd.addParameter("size", 100);
            }
        }
        else if (fd instanceof SelectFieldDescriptor)
        {
            SelectFieldDescriptor select = (SelectFieldDescriptor) fd;
            if (select.getList() == null)
            {
                addDefaultOptions(parentPath, baseName, property, select);
            }
        }

        for(Validator validator: validators)
        {
            if(validator instanceof FieldValidator && ((FieldValidator)validator).getFieldName().equals(fd.getName()))
            {
                fd.setConstrained(true);
                if(validator instanceof RequiredValidator)
                {
                    fd.setRequired(true);
                }
            }
        }
    }

    private void addDefaultOptions(String parentPath, String baseName, TypeProperty typeProperty, SelectFieldDescriptor fd)
    {
        if (typeProperty.getType().getTargetType() instanceof EnumType)
        {
            OptionAnnotationHandler.process(configurationProvider, new EnumOptionProvider(), parentPath, baseName, fd);
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
     * @param type        the composite type that has been annotated (or meta-annotated)
     * @param descriptor  the target that will be modified by these annotations.
     * @param annotations the annotations that need to be processed (includes meta already).
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

            Handler handlerAnnotation = annotationType.getAnnotation(Handler.class);
            if (handlerAnnotation != null)
            {
                try
                {
                    AnnotationHandler handler = objectFactory.buildBean(handlerAnnotation.className(), AnnotationHandler.class);
                    handler.process(type, annotation, descriptor);
                }
                catch (Exception e)
                {
                    LOG.warning("Unexpected exception processing the annotation handler.", e);
                }
            }
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationValidatorProvider(ConfigurationValidatorProvider configurationValidatorProvider)
    {
        this.configurationValidatorProvider = configurationValidatorProvider;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
