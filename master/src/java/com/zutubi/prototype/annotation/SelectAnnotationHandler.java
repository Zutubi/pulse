package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.Type;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.i18n.Messages;

import java.lang.annotation.Annotation;

/**
 *
 *
 */
public class SelectAnnotationHandler extends FieldAnnotationHandler
{
    private ObjectFactory objectFactory;

    public void process(Annotation annotation, Descriptor descriptor)
    {
        super.process(annotation, descriptor);
        
        try
        {
            Class optionProviderClass = ((Select)annotation).optionProvider();
            OptionProvider optionProvider = objectFactory.buildBean(optionProviderClass);

            java.util.List<String> optionList = optionProvider.getOptions();
            descriptor.addParameter("list", optionList);

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
