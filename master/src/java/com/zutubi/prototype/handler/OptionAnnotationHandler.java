package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.model.SelectFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 */
public abstract class OptionAnnotationHandler extends FieldAnnotationHandler
{
    private ConfigurationTemplateManager configurationTemplateManager;
    /**
     * Object factory provides access to object instantiation services.
     */
    private ObjectFactory objectFactory;

    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        // do everything that the standard field annotation handler does,
        super.process(annotatedType, annotation, descriptor);

        //  and then a little bit extra.
        String className = getOptionProviderClass(annotation);
        OptionProvider optionProvider = (OptionProvider) objectFactory.buildBean(ClassLoaderUtils.loadAssociatedClass(annotatedType.getClazz(), className));

        SelectFieldDescriptor field = (SelectFieldDescriptor) descriptor;
        process(configurationTemplateManager, optionProvider, field.getParentPath(), field.getBaseName(), field);
    }

    public static void process(ConfigurationTemplateManager configurationTemplateManager, OptionProvider optionProvider, String parentPath, String baseName, SelectFieldDescriptor field)
    {
        Configuration instance = null;
        if(baseName != null)
        {
            instance = configurationTemplateManager.getInstance(PathUtils.getPath(parentPath, baseName));
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

    /**
     * Required resource.
     *
     * @param objectFactory instance
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
