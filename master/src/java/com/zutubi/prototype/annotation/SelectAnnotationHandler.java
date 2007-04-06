package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 *
 */
public class SelectAnnotationHandler extends FieldAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(SelectAnnotationHandler.class);
    
    /**
     * Object factory provides access to object instantiation services.
     */
    private ObjectFactory objectFactory;

    public void process(Annotation annotation, Descriptor descriptor)
    {
        // do everything that the standard field annotation handler does,
        super.process(annotation, descriptor);

        //  and then a little bit extra.
        try
        {
            Class optionProviderClass = ((Select)annotation).optionProvider();
            OptionProvider optionProvider = objectFactory.buildBean(optionProviderClass);

            List<String> optionList = optionProvider.getOptions();
            descriptor.addParameter("list", optionList);
        }
        catch (Exception e)
        {
            LOG.warning(e);
        }
    }

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
