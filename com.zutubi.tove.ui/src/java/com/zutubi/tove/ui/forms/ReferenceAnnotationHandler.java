package com.zutubi.tove.ui.forms;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.OptionFieldModel;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.logging.Logger;
import freemarker.template.TemplateException;

import java.io.IOException;

/**
 * Handler for processing reference properties.  Adds the list to select from
 * to the descriptor.
 */
public class ReferenceAnnotationHandler extends OptionAnnotationHandler
{
    private static final Logger LOG = Logger.getLogger(ReferenceAnnotationHandler.class);

    private freemarker.template.Configuration freemarkerConfiguration;

    @Override
    protected void process(TypeProperty property, OptionFieldModel field, OptionProvider optionProvider, FormContext context)
    {
        // In the case of the @Reference annotation, the presence of the 'dependentOn' field is used to
        // replace the default 'instance' for use by the OptionProvider.  Note that the configured OptionProvider
        // will need to expect that it will not be receiving the default instance.
        Configuration scopeInstance = context.getExistingInstance();
        if (field.hasParameter("dependentOn"))
        {
            String dependentField = (String) field.getParameter("dependentOn");
            if (scopeInstance != null)
            {
                try
                {
                    scopeInstance = (Configuration) BeanUtils.getProperty(dependentField, scopeInstance);
                }
                catch (BeanException e)
                {
                    // This is caused by an annotation configuration problem.  Would be good to catch
                    // this earlier.
                    LOG.warning("Failed to retrieve dependent property.", e);
                }
            }

            // The user can later change the dependentOn field's value, so we need to track changes and reload our
            // options.
            try
            {
                FieldScriptAnnotationHandler.loadTemplate(ReferenceAnnotationHandler.class, field, ReferenceAnnotationHandler.class.getSimpleName() + ".trackdependent", freemarkerConfiguration);
            }
            catch (IOException | TemplateException e)
            {
                LOG.warning("Unable to add dependent tracking script: " + e.getMessage(), e);
            }
        }
        else if (scopeInstance == null)
        {
            scopeInstance = configurationProvider.get(context.getClosestExistingPath(), Configuration.class);
        }

        FormContext scopeContext = scopeInstance == null ? new FormContext(context.getClosestExistingPath()) : new FormContext(scopeInstance);
        super.process(property, field, optionProvider, scopeContext);
    }

    public void setFreemarkerConfiguration(freemarker.template.Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
