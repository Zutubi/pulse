package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.RenderContext;
import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class BodyUIComponent extends UIComponent
{
    private List<Component> nestedComponents;

/*
    public boolean start(ComponentRenderer renderer) throws Exception
    {
        // generate the template renderer context
        evaluateParameters();

        TemplateRendererContext templateContext = new TemplateRendererContext(getDefaultOpenTemplate(), getParameters(), context);
        context.getRenderer().render(templateContext);

        return true;
    }

    public void body(ComponentRenderer renderer) throws Exception
    {
        for (Component nestedComponent : getNestedComponents())
        {
            renderer.render(nestedComponent);
        }
    }
*/

    public abstract String getDefaultOpenTemplate();


    public void render(RenderContext context, TemplateRenderer renderer) throws Exception
    {
        setContext(context);
        
        // generate the template renderer context
        evaluateParameters();

        TemplateRendererContext templateContext = new TemplateRendererContext(getDefaultOpenTemplate(), getParameters(), context);
        context.getRenderer().render(templateContext);

        for (Component nestedComponent : getNestedComponents())
        {
            nestedComponent.setContext(context);
            nestedComponent.render(context, renderer);
        }

        templateContext = new TemplateRendererContext(getDefaultTemplate(), getParameters(), context);
        context.getRenderer().render(templateContext);
    }

    public List<Component> getNestedComponents()
    {
        if (nestedComponents == null)
        {
            nestedComponents = new LinkedList<Component>();
        }
        return nestedComponents;
    }

    public void addNestedComponent(Component component)
    {
        component.parent = this;
        getNestedComponents().add(component);
    }

    public Component getNestedComponent(String id)
    {
        if (id == null)
        {
            return null;
        }
        for (Component component : getNestedComponents())
        {
            if (id.equals(component.getId()))
            {
                return component;
            }
        }
        return null;
    }

}
