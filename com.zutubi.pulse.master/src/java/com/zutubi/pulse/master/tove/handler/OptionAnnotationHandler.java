package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.rest.model.forms.OptionFieldModel;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Handles annotations for field types that present a list of options to the
 * user.  Uses an {@link com.zutubi.pulse.master.tove.handler.OptionProvider}
 * to get the list of options.
 */
public class OptionAnnotationHandler extends FieldAnnotationHandler
{
    private ObjectFactory objectFactory;
    protected ConfigurationProvider configurationProvider;

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return true;
    }

    // FIXME kendo old version
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        super.process(annotatedType, annotation, descriptor);

        OptionFieldDescriptor field = (OptionFieldDescriptor) descriptor;
        if (!field.isLazy())
        {
            OptionProvider optionProvider = OptionProviderFactory.build(annotatedType, field.getProperty().getType(), annotation, objectFactory);
            Configuration instance = null;
            String baseName = field.getBaseName();
            if(baseName != null && configurationProvider != null)
            {
                instance = configurationProvider.get(PathUtils.getPath(field.getParentPath(), baseName), Configuration.class);
            }

            process(field, optionProvider, instance);
        }
    }

    protected void process(OptionFieldDescriptor field, OptionProvider optionProvider, Object instance)
    {
        String parentPath = field.getParentPath();
        TypeProperty fieldTypeProperty = field.getProperty();

        FormContext context = new FormContext((Configuration) instance);
        List optionList = optionProvider.getOptions(fieldTypeProperty, context);
        field.setList(optionList);

        Object emptyOption = optionProvider.getEmptyOption(fieldTypeProperty, context);
        if (emptyOption != null)
        {
            field.setEmptyOption(emptyOption);
        }

        String key = optionProvider.getOptionValue();
        if (key != null)
        {
            field.setListKey(key);
        }

        String value = optionProvider.getOptionText();
        if (value != null)
        {
            field.setListValue(value);
        }
    }



    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        super.process(annotatedType, property, annotation, field, context);

        OptionFieldModel optionField = (OptionFieldModel) field;
        if (!optionField.isLazy())
        {
            OptionProvider optionProvider = OptionProviderFactory.build(annotatedType, property.getType(), annotation, objectFactory);
            process(property, optionField, optionProvider, context);
        }
    }

    protected void process(TypeProperty property, OptionFieldModel field, OptionProvider optionProvider, FormContext context)
    {
        List optionList = optionProvider.getOptions(property, context);
        field.setList(optionList);

        Object emptyOption = optionProvider.getEmptyOption(property, context);
        if (emptyOption != null)
        {
            field.setEmptyOption(emptyOption);
        }

        String key = optionProvider.getOptionValue();
        if (key != null)
        {
            field.setListValue(key);
        }

        String value = optionProvider.getOptionText();
        if (value != null)
        {
            field.setListText(value);
        }
    }


    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
