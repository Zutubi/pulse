package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.logging.Logger;

import java.lang.annotation.Annotation;

/**
 * Handler for processing reference properties.  Adds the list to select from
 * to the descriptor.
 */
public class ReferenceAnnotationHandler extends OptionAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(ReferenceAnnotationHandler.class);

    @Override
    protected void process(OptionFieldDescriptor field, OptionProvider optionProvider, Object instance)
    {
        // In the case of the @Reference annotation, the presence of the 'dependentOn' field is used to
        // replace the default 'instance' for use by the OptionProvider.  Note that the configured OptionProvider
        // will need to expect that it will not be receiving the default instance.
        if (field.hasParameter("dependentOn"))
        {
            String dependentField = (String) field.getParameter("dependentOn");
            if (instance != null)
            {
                try
                {
                    instance = BeanUtils.getProperty(dependentField, instance);
                }
                catch (BeanException e)
                {
                    // This is caused by an annotation configuration problem.  Would be good to catch
                    // this earlier.
                    LOG.warning("Failed to retrieve dependent property.", e);
                }
            }
        }

        super.process(field, optionProvider, instance);
    }

    protected String getOptionProviderClass(Annotation annotation)
    {
        return ((Reference)annotation).optionProvider();
    }
}
