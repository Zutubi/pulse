package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.tove.model.OptionFieldDescriptor;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.logging.Logger;

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
        else if (instance == null)
        {
            // The option provider needs an instance to define the scope where reference-able instances can be found.
            // If we don't have one it's because we're adding a new instance, in that case use the parent of the new
            // instance as the scope.
            instance = configurationProvider.get(field.getParentPath(), Configuration.class);
        }

        super.process(field, optionProvider, instance);
    }
}
