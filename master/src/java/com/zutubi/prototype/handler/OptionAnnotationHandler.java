package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.model.SelectFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.util.bean.ObjectFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;

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
        String fieldPath = field.getPath();
        process(configurationTemplateManager, optionProvider, fieldPath, field);
    }

    public static void process(ConfigurationTemplateManager configurationTemplateManager, OptionProvider optionProvider, String fieldPath, SelectFieldDescriptor field)
    {
        String instancePath = PathUtils.getParentPath(fieldPath);
        Object instance = configurationTemplateManager.getInstance(instancePath);
        TypeProperty fieldTypeProperty = field.getProperty();

        // FIXME: not sure what the correct behaviour here should be? see comment.
        // there are occasions, particularly with wizards, where we are rendering a configuration object at
        // a path that represents a collection of that type.  In that situation, if the option provider is expecting
        // the type that it is defined on, then things will go amiss.  How should we deal with this? pass a null?

        Collection optionList = optionProvider.getOptions(instance, fieldPath, fieldTypeProperty);
        field.setList(optionList);
        if (optionProvider.getOptionKey() != null)
        {
            field.setListKey(optionProvider.getOptionKey());
        }
        if (optionProvider.getOptionValue() != null)
        {
            field.setListValue(optionProvider.getOptionValue());
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
