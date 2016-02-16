package com.zutubi.pulse.master.rest;

import com.google.common.base.Function;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.rest.model.forms.*;
import com.zutubi.pulse.master.tove.config.EnumOptionProvider;
import com.zutubi.pulse.master.tove.handler.AnnotationHandler;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Handler;
import com.zutubi.tove.annotations.NoInherit;
import com.zutubi.tove.config.ConfigurationValidatorProvider;
import com.zutubi.tove.type.*;
import com.zutubi.tove.validation.NameValidator;
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
 * FIXME kendo it feels like this belongs in Tove (along with the models) as it is not really
 *       specific to the RESTish API: it just makes a model that is not dependent on the backend
 *       (and thus is suitable to serialise for any purpose).  However, some other models are
 *       really just ways to represent things in an API-friendly manner, so the question is
 *       whether it is better to keep forms with them (all models together)?
 */
public class FormModelBuilder
{
    private static final Logger LOG = Logger.getLogger(FormModelBuilder.class);

    static final String PARAMETER_NO_INHERIT = "noInherit";

    /**
     * The object factory is required for the instantiation of objects that occurs within the form descriptor.
     * To ensure that this always works, we default to a base implementation of the Object factory, which simply
     * instantiated objects.  When deployed, this should be replaced by the auto wiring object factory.
     */
    private ObjectFactory objectFactory = new DefaultObjectFactory();

    private static final Map<Class, String> DEFAULT_FIELD_TYPE_MAPPING = new HashMap<>();

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
    private ConfigurationValidatorProvider configurationValidatorProvider;

    public FormModelBuilder()
    {
        // FIXME kendo: There is a duplication here: the default constructors set these types, and
        // we also pair them with the classes here.
        registerFieldType(FieldType.CHECKBOX, CheckboxFieldModel.class);
        registerFieldType(FieldType.COMBOBOX, ComboboxFieldModel.class);
        registerFieldType(FieldType.CONTROLLING_CHECKBOX, ControllingCheckboxFieldModel.class);
        registerFieldType(FieldType.CONTROLLING_SELECT, ControllingSelectFieldModel.class);
        registerFieldType(FieldType.DROPDOWN, DropdownFieldModel.class);
        registerFieldType(FieldType.HIDDEN, HiddenFieldModel.class);
        registerFieldType(FieldType.ITEM_PICKER, ItemPickerFieldModel.class);
        registerFieldType(FieldType.PASSWORD, PasswordFieldModel.class);
        registerFieldType(FieldType.STRING_LIST, StringListFieldModel.class);
        registerFieldType(FieldType.TEXT, TextFieldModel.class);
        registerFieldType(FieldType.TEXTAREA, TextAreaFieldModel.class);
    }

    private void registerFieldType(String type, Class<? extends FieldModel> clazz)
    {
        fieldDescriptorTypes.put(type, clazz);
    }

    /**
     * Creates a form for configuring the given type.  This method deliberately only deals with the
     * type without further context (e.g. an existing instance).  Therefore it does not include
     * things like option lists, validation notes or other decoration that depends on the context.
     *
     * @param type type to create the form for
     * @return the new form
     */
    public FormModel createForm(CompositeType type)
    {
        Messages messages = Messages.getInstance(type.getClazz());
        FormModel form = new FormModel();

        Form formAnnotation = type.getAnnotation(Form.class, true);
        List<String> fieldOrder = new ArrayList<>();
        if (formAnnotation != null)
        {
            AnnotationUtils.setPropertiesFromAnnotation(formAnnotation, form);
            fieldOrder.addAll(Arrays.asList(formAnnotation.fieldOrder()));
        }
        addFields(type, messages, form);

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

    void applyContextToForm(FormContext context, CompositeType type, FormModel form)
    {
        for (FieldModel field: form.getFields())
        {
            handleAnnotations(type, type.getProperty(field.getName()), field, context);
        }
    }

    private boolean hasRequiredValidator(String fieldName, List<Validator> validators)
    {
        for (Validator v : validators)
        {
            if (v instanceof FieldValidator && ((FieldValidator) v).getFieldName().equals(fieldName))
            {
                return v instanceof RequiredValidator || v instanceof NameValidator;
            }
        }

        return false;
    }

    private void addFields(CompositeType type, Messages messages, FormModel form)
    {
        List<Validator> validators =  getValidators(type);

        for (String propertyName: type.getSimplePropertyNames())
        {
            TypeProperty property = type.getProperty(propertyName);
            if (property.getType() instanceof SimpleType)
            {
                FieldModel fd = createField(property, messages);
                addFieldParameters(type, property, fd, validators);
                form.addField(fd);
            }
            else
            {
                String fieldType = FieldType.ITEM_PICKER;
                com.zutubi.tove.annotations.Field field = AnnotationUtils.findAnnotation(property.getAnnotations(), com.zutubi.tove.annotations.Field.class);
                if (field != null && !field.type().equals(FieldType.DROPDOWN))
                {
                    fieldType = field.type();
                }

                FieldModel fd = createFieldOfType(fieldType, property, messages);
                addFieldParameters(type, property, fd, validators);
                form.addField(fd);
            }
        }
    }

    private List<Validator> getValidators(CompositeType type)
    {
        List<Validator> validators;
        try
        {
            validators = configurationValidatorProvider.getValidators(type.getClazz());
        }
        catch (Throwable e)
        {
            // Not ideal, but we can soldier on regardless.
            LOG.warning("Unable to get validators for type '" + type.getSymbolicName() + "': " + e.getMessage(), e);
            validators = Collections.emptyList();
        }
        return validators;
    }

    private FieldModel createField(TypeProperty property, Messages messages)
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
                fieldType = FieldType.DROPDOWN;
            }
        }

        return createFieldOfType(fieldType, property, messages);
    }

    private FieldModel createFieldOfType(String type, TypeProperty property, Messages messages)
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

        field.setName(property.getName());
        field.setType(type);
        field.setLabel(messages.format(property.getName() + ".label"));
        return field;
    }

    private void addFieldParameters(CompositeType type, TypeProperty property, FieldModel field, List<Validator> validators)
    {
        field.setRequired(hasRequiredValidator(field.getName(), validators));
        if (property.getAnnotation(NoInherit.class) != null)
        {
            field.addParameter(PARAMETER_NO_INHERIT, Boolean.toString(true));
        }

        handleAnnotations(type, property, field, null);

        if (!property.isWritable())
        {
            field.setReadOnly(true);
        }

        if (field instanceof TextFieldModel)
        {
            Numeric numeric = AnnotationUtils.findAnnotation(property.getAnnotations(), Numeric.class);
            if (numeric != null)
            {
                ((TextFieldModel) field).setSize(100);
            }
        }
        else if (field instanceof OptionFieldModel)
        {
            OptionFieldModel optionModel = (OptionFieldModel) field;
            if (optionModel.getList() == null)
            {
                addDefaultOptions(property, optionModel);
            }
        }
    }

    private void addDefaultOptions(TypeProperty typeProperty, OptionFieldModel fd)
    {
        if (typeProperty.getType().getTargetType() instanceof EnumType)
        {
            // We can pass null through to the option provider here because we know that the EnumOptionProvider
            // does not make use of the context.
            EnumOptionProvider optionProvider = new EnumOptionProvider();
            fd.setList(optionProvider.getOptions(typeProperty, null));
            fd.setListValue(optionProvider.getOptionValue());
            fd.setListText(optionProvider.getOptionText());

            Object emptyOption = optionProvider.getEmptyOption(typeProperty, null);
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

    private void handleAnnotations(CompositeType type, TypeProperty property, FieldModel field, FormContext context)
    {
        for (Annotation annotation : property.getAnnotations())
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
                    boolean contextAvailable = context != null;
                    boolean contextRequired = handler.requiresContext(annotation);
                    if (contextAvailable == contextRequired)
                    {
                        handler.process(type, property, annotation, field, context);
                    }
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

    public void setConfigurationValidatorProvider(ConfigurationValidatorProvider configurationValidatorProvider)
    {
        this.configurationValidatorProvider = configurationValidatorProvider;
    }
}
