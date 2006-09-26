package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.ui.components.Component;

/**
 * <class-comment/>
 */
public class ComponentRenderer
{
    private TemplateRenderer renderer;

    public void render(Component component) throws Exception
    {
        // create the render context that defines the context in
        // which the component is being rendered.
        RenderContext context = new RenderContext(renderer);
        component.setContext(context);

        if (component.start())
        {
            evaluateBody(component);
        }

        while (component.end())
        {
            evaluateBody(component);
        }
    }

    private void evaluateBody(Component component) throws Exception
    {
        for (Component nestedComponent : component.getNestedComponents())
        {
            render(nestedComponent);
        }
    }

    public void setTemplateRenderer(TemplateRenderer renderer)
    {
        this.renderer = renderer;
    }
}
