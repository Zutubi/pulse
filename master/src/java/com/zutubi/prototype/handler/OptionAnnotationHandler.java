package com.zutubi.prototype.handler;

import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.ClassLoaderUtils;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 *
 */
public abstract class OptionAnnotationHandler extends FieldAnnotationHandler
{
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

        FieldDescriptor field = (FieldDescriptor) descriptor;
        Collection optionList = optionProvider.getOptions(field.getPath(), field.getProperty());
        descriptor.addParameter("list", optionList);
        if (optionProvider.getOptionKey() != null)
        {
            descriptor.addParameter("listKey", optionProvider.getOptionKey());
        }
        if (optionProvider.getOptionValue() != null)
        {
            descriptor.addParameter("listValue", optionProvider.getOptionValue());
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
}
