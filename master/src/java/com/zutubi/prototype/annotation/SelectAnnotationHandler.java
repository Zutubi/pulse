package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.pulse.core.ObjectFactory;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class SelectAnnotationHandler implements AnnotationHandler
{
    private ObjectFactory objectFactory;

    public void process(Annotation annotation, Descriptor descriptor)
    {
        try
        {
            Class optionProviderClass = ((Select)annotation).value();
            OptionProvider optionProvider = objectFactory.buildBean(optionProviderClass);
            descriptor.addParameter("list", optionProvider.getOptions());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
