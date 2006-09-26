package com.zutubi.pulse.form.ui;

/**
 * <class-comment/>
 */
public class RenderContext
{
    private TemplateRenderer renderer;

    public RenderContext(TemplateRenderer renderer)
    {
        this.renderer = renderer;
    }

    public TemplateRenderer getRenderer()
    {
        return renderer;
    }
}
