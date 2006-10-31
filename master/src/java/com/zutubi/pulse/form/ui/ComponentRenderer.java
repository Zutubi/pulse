package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.ui.components.Component;

/**
 * <class-comment/>
 */
public class ComponentRenderer
{
    private RenderContext context;

    public void setContext(RenderContext context)
    {
        this.context = context;
    }

    public void render(Component component) throws Exception
    {
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
}
