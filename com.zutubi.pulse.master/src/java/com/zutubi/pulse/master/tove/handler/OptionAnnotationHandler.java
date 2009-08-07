package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.tove.config.EmptyOptionProvider;
import com.zutubi.pulse.master.tove.config.EnumOptionProvider;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.EnumType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public abstract class OptionAnnotationHandler extends FieldAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(OptionAnnotationHandler.class);

    /**
     * Object factory provides access to object instantiation services.
     */
    private ObjectFactory objectFactory;
    private ConfigurationProvider configurationProvider;

    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        // Do everything that the standard field annotation handler does,
        super.process(annotatedType, annotation, descriptor);

        OptionFieldDescriptor field = (OptionFieldDescriptor) descriptor;
        OptionProvider optionProvider;

        // And then a little bit extra.
        String className = getOptionProviderClass(annotation);
        if(!StringUtils.stringSet(className))
        {
            if(field.getProperty().getType() instanceof EnumType)
            {
                optionProvider = new EnumOptionProvider();
            }
            else
            {
                optionProvider = new EmptyOptionProvider();
            }
        }
        else
        {
            optionProvider = (OptionProvider) objectFactory.buildBean(ClassLoaderUtils.loadAssociatedClass(annotatedType.getClazz(), className));
        }

        String parentPath = field.getParentPath();
        String baseName = field.getBaseName();

        Object instance = null;
        if (baseName != null && configurationProvider != null) // may be null during the setup since we are rendering forms but the configuration system is not yet available.
        {
            instance = configurationProvider.get(PathUtils.getPath(parentPath, baseName), Configuration.class);
        }

        process(field, optionProvider, parentPath, instance, field.getProperty());
    }

    protected void process(OptionFieldDescriptor field, OptionProvider optionProvider, String parentPath, Object instance, TypeProperty fieldTypeProperty)
    {
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

    protected abstract String getOptionProviderClass(Annotation annotation);

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
