package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.TemplateRendererContext;

/**
 * <class-comment/>
 */
public abstract class BodyUIComponent extends UIComponent
{
    public boolean start() throws Exception
    {
        // generate the template renderer context
        evaluateParameters();

        TemplateRendererContext templateContext = new TemplateRendererContext(getDefaultOpenTemplate(), getParameters());
        context.getRenderer().render(templateContext);

        return true;
    }

    public abstract String getDefaultOpenTemplate();
}
