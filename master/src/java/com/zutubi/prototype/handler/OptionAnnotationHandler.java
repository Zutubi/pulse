package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.model.OptionFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.EnumType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.prototype.config.EmptyOptionProvider;
import com.zutubi.pulse.prototype.config.EnumOptionProvider;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public abstract class OptionAnnotationHandler extends FieldAnnotationHandler
{
    /**
     * Object factory provides access to object instantiation services.
     */
    private ObjectFactory objectFactory;
    private ConfigurationProvider configurationProvider;

    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        // do everything that the standard field annotation handler does,
        super.process(annotatedType, annotation, descriptor);

        OptionFieldDescriptor field = (OptionFieldDescriptor) descriptor;
        OptionProvider optionProvider;

        //  and then a little bit extra.
        String className = getOptionProviderClass(annotation);
        if(!TextUtils.stringSet(className))
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

        process(configurationProvider, optionProvider, field.getParentPath(), field.getBaseName(), field);
    }

    public static void process(ConfigurationProvider configurationProvider, OptionProvider optionProvider, String parentPath, String baseName, OptionFieldDescriptor field)
    {
        Configuration instance = null;
        if(baseName != null && configurationProvider != null)
        {
            instance = configurationProvider.get(PathUtils.getPath(parentPath, baseName), Configuration.class);
        }

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
