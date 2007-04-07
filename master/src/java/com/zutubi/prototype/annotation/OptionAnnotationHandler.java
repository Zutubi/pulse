package com.zutubi.prototype.annotation;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.FieldDescriptor;

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

    public void process(Annotation annotation, Descriptor descriptor) throws Exception
    {
        // do everything that the standard field annotation handler does,
        super.process(annotation, descriptor);

        //  and then a little bit extra.
        OptionProvider optionProvider = objectFactory.buildBean(getOptionProviderClass(annotation));

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

    protected abstract Class<? extends OptionProvider> getOptionProviderClass(Annotation annotation);

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
