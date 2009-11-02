package com.zutubi.pulse.master.tove.handler;

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
public abstract class OptionAnnotationHandler extends FieldAnnotationHandler
{
    private ObjectFactory objectFactory;
    private ConfigurationProvider configurationProvider;

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
        
        List optionList = optionProvider.getOptions(instance, parentPath, fieldTypeProperty);
        field.setList(optionList);

        Object emptyOption = optionProvider.getEmptyOption(instance, parentPath, fieldTypeProperty);
        if (emptyOption != null)
        {
            field.setEmptyOption(emptyOption);
        }

        String key = optionProvider.getOptionKey();
        if (key != null)
        {
            field.setListKey(key);
        }

        String value = optionProvider.getOptionValue();
        if (value != null)
        {
            field.setListValue(value);
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
