package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.i18n.TextProvider;

/**
 * <class-comment/>
 */
public class RenderContext
{
    private TemplateRenderer renderer;

    private TextProvider textProvider;

    public RenderContext(TemplateRenderer renderer, TextProvider provider)
    {
        this.renderer = renderer;
        this.textProvider = provider;
    }

    public TemplateRenderer getRenderer()
    {
        return renderer;
    }

    public String getText(String key)
    {
        return textProvider.getText(key);
    }
}
