package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.model.forms.*;
import com.zutubi.pulse.master.tove.config.EnumOptionProvider;
import com.zutubi.pulse.master.tove.handler.AnnotationHandler;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Handler;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.tove.config.ConfigurationValidatorProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
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

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Builds {@link FormModel} instances out of annotated type information.
 *
 * FIXME kendo this replaces FormDescriptorFactory (FormModel replaces FormDescriptor, and Form
 *       dies because we instantiate on the client)
 *
 * FIXME kendo it feels like this belongs in Tove (along with the models) as it is not really
 *       specific to the RESTish API: it just makes a model that is not dependent on the backend
 *       (and thus is suitable to serialise for any purpose).  However, some other models are
 *       really just ways to represent things in an API-friendly manner, so the question is
 *       whether it is better to keep forms with them (all models together)?
 */
public class FormModelBuilder
{
    private static final Logger LOG = Logger.getLogger(FormModelBuilder.class);

    /**
     * The object factory is required for the instantiation of objects that occurs within the form descriptor.
     * To ensure that this always works, we default to a base implementation of the Object factory, which simply
     * instantiated objects.  When deployed, this should be replaced by the auto wiring object factory.
     */
    private ObjectFactory objectFactory = new DefaultObjectFactory();

    private static final Map<Class, String> DEFAULT_FIELD_TYPE_MAPPING = new HashMap<Class, String>();

    static
    {
        DEFAULT_FIELD_TYPE_MAPPING.put(String.class, FieldType.TEXT);
        DEFAULT_FIELD_TYPE_MAPPING.put(File.class, FieldType.TEXT);
        DEFAULT_FIELD_TYPE_MAPPING.put(Boolean.class, FieldType.CHECKBOX);
        DEFAULT_FIELD_TYPE_MAPPING.put(Boolean.TYPE, FieldType.CHECKBOX);
        DEFAULT_FIELD_TYPE_MAPPING.put(Integer.class, FieldType.TEXT);
        DEFAULT_FIELD_TYPE_MAPPING.put(Integer.TYPE, FieldType.TEXT);
        DEFAULT_FIELD_TYPE_MAPPING.put(Long.class, FieldType.TEXT);
        DEFAULT_FIELD_TYPE_MAPPING.put(Long.TYPE, FieldType.TEXT);
    }

    private Map<String, Class<? extends FieldModel>> fieldDescriptorTypes = new HashMap<>();
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationValidatorProvider configurationValidatorProvider;

    public FormModelBuilder()
    {
        // FIXME kendo: There is a duplication here: the default constructors set these types, and
        // we also pair them with the classes here.
        registerFieldType(FieldType.CHECKBOX, CheckboxFieldModel.class);
        registerFieldType(FieldType.CONTROLLING_CHECKBOX, ControllingCheckboxFieldModel.class);
        registerFieldType(FieldType.CONTROLLING_SELECT, ControllingSelectFieldModel.class);
        registerFieldType(FieldType.HIDDEN, HiddenFieldModel.class);
        registerFieldType(FieldType.PASSWORD, PasswordFieldModel.class);
        registerFieldType(FieldType.SELECT, SelectFieldModel.class);
        registerFieldType(FieldType.ITEM_PICKER, ItemPickerFieldModel.class);
        registerFieldType(FieldType.TEXT, TextFieldModel.class);
        registerFieldType(FieldType.TEXTAREA, TextAreaFieldModel.class);
    }

    public void registerFieldType(String type, Class<? extends FieldModel> clazz)
    {
        fieldDescriptorTypes.put(type, clazz);
    }

    public FormModel createForm(String parentPath, String baseName, CompositeType type, boolean concrete, String name)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        FormModel form = new FormModel(name, type.getClazz().getName(), type.getSymbolicName());
        form.setActions(Arrays.asList("save", "cancel"));

        Form formAnnotation = type.getAnnotation(Form.class, true);
        List<String> fieldOrder = new ArrayList<>();
        if (formAnnotation != null)
        {
            AnnotationUtils.setPropertiesFromAnnotation(formAnnotation, form);
            fieldOrder.addAll(Arrays.asList(formAnnotation.fieldOrder()));
        }
        addFields(parentPath, baseName, type, messages, concrete, form);

        transformOptionFieldTypes(form);

        fieldOrder = ToveUtils.evaluateFieldOrder(fieldOrder, newArrayList(transform(form.getFields(), new Function<FieldModel, String>()
        {
            public String apply(FieldModel field)
            {
                return field.getName();
            }
        })));

        form.sortFields(fieldOrder);

        return form;
    }

    private void addFields(String parentPath, String baseName, CompositeType type, Messages messages, boolean concrete, FormModel form)
    {
        List<Validator> validators = getValidators(parentPath, baseName, concrete, type);
        String path = PathUtils.getPath(parentPath, baseName);

        for (TypeProperty property : type.getProperties(SimpleType.class))
        {
            FieldModel fd = createField(path, property, messages);
            addFieldParameters(type, parentPath, property, fd, validators);
            form.addField(fd);
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

                FieldModel fd = createFieldOfType(fieldType, path, property, messages);
                if (fd instanceof OptionFieldModel)
                {
                    ((OptionFieldModel) fd).setMultiple(true);
                }
                addFieldParameters(type, parentPath, property, fd, validators);
                form.addField(fd);
            }
        }
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
            validators = new ArrayList<>(0);
        }
        return validators;
    }

    private FieldModel createField(String path, TypeProperty property, Messages messages)
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
                fieldType = DEFAULT_FIELD_TYPE_MAPPING.get(propertyType.getClazz());
            }
            else if (propertyType instanceof EnumType)
            {
                fieldType = FieldType.SELECT;
            }
        }

        return createFieldOfType(fieldType, path, property, messages);
    }

    private FieldModel createFieldOfType(String type, String path, TypeProperty property, Messages messages)
    {
        Class<? extends FieldModel> clazz = fieldDescriptorTypes.get(type);
        FieldModel field;
        if (clazz == null)
        {
            field = new FieldModel();
        }
        else
        {
            try
            {
                field = clazz.newInstance();
            }
            catch (Exception e)
            {
                LOG.severe(e);
                field = new FieldModel();
            }
        }

        field.setPath(path);
        field.setName(property.getName());
        field.setType(type);
        field.setLabel(messages.format(property.getName() + ".label"));
        return field;
    }

    private void addFieldParameters(CompositeType type, String parentPath, TypeProperty property, FieldModel field, List<Validator> validators)
    {
        handleAnnotations(type, property, field, property.getAnnotations());

        if (!property.isWritable())
        {
            field.setReadOnly(true);
        }

        // FIXME kendo these two special cases look like they should be handled elsewhere?
        if (field instanceof TextFieldModel)
        {
            Numeric numeric = AnnotationUtils.findAnnotation(property.getAnnotations(), Numeric.class);
            if (numeric != null)
            {
                ((TextFieldModel) field).setSize(100);
            }
        }
        else if (field instanceof SelectFieldModel)
        {
            SelectFieldModel select = (SelectFieldModel) field;
            if (select.getList() == null)
            {
                addDefaultOptions(parentPath, property, select);
            }
        }

        for (Validator validator: validators)
        {
            if (validator instanceof RequiredValidator && ((FieldValidator)validator).getFieldName().equals(field.getName()))
            {
                field.setRequired(true);
            }
        }
    }

    private void addDefaultOptions(String parentPath, TypeProperty typeProperty, SelectFieldModel fd)
    {
        if (typeProperty.getType().getTargetType() instanceof EnumType)
        {
            // We can pass null through to the option provider here because we know that the EnumOptionProvider
            // does not make use of the instance.
            EnumOptionProvider optionProvider = new EnumOptionProvider();
            fd.setList(optionProvider.getOptions(null, parentPath, typeProperty));
            fd.setListValue(optionProvider.getOptionValue());
            fd.setListText(optionProvider.getOptionText());

            Object emptyOption = optionProvider.getEmptyOption(null, parentPath, typeProperty);
            if (emptyOption != null)
            {
                fd.setEmptyOption(emptyOption);
            }
        }
        else
        {
            fd.setList(Collections.EMPTY_LIST);
        }
    }

    private void handleAnnotations(CompositeType type, TypeProperty property, FieldModel field, Iterable<Annotation> annotations)
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
                    handler.process(type, property, annotation, field);
                }
                catch (Exception e)
                {
                    LOG.warning("Unexpected exception processing the annotation handler.", e);
                }
            }
        }
    }

    private void transformOptionFieldTypes(FormModel form)
    {
        // FIXME kendo: this is a hack to replace something that happened on instantiate previously.
        // We need some other way for this to happen, maybe in the annotation handlers?
        for (FieldModel field: form.getFields())
        {
            if (field instanceof OptionFieldModel && !(field instanceof ControllingSelectFieldModel))
            {
                OptionFieldModel optionFieldModel = (OptionFieldModel) field;
                if (!optionFieldModel.isMultiple() && optionFieldModel.getSize() <= 1)
                {
                    if (optionFieldModel.isEditable())
                    {
                        optionFieldModel.setType(FieldType.COMBOBOX);
                    }
                    else
                    {
                        optionFieldModel.setType(FieldType.DROPDOWN);
                    }
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
}
