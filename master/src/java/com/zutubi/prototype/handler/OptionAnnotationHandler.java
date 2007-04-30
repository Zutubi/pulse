package com.zutubi.prototype.handler;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.model.SelectFieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
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
    private ConfigurationPersistenceManager configurationPersistenceManager;
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
        process(configurationPersistenceManager, optionProvider, fieldPath, field);
    }

    public static void process(ConfigurationPersistenceManager configurationPersistenceManager, OptionProvider optionProvider, String fieldPath, SelectFieldDescriptor field)
    {
        String instancePath = PathUtils.getParentPath(fieldPath);
        Collection optionList = optionProvider.getOptions(configurationPersistenceManager.getInstance(instancePath), fieldPath, field.getProperty());
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

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
