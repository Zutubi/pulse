package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.Renderer;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class BodyComponent extends Component
{
    private List<Component> nestedComponents = new LinkedList<Component>();

    private boolean renderedNestedComponents = false;

    public void addNested(Component component)
    {
        this.nestedComponents.add(component);
    }

    public List<Component> getNestedComponents()
    {
        return nestedComponents;
    }

    public Component getNestedComponent(String name)
    {
        if (name == null)
        {
            return null;
        }
        for (Component c : nestedComponents)
        {
            if (name.equals(c.getName()))
            {
                return c;
            }
        }
        return null;
    }

    public void render(Renderer r)
    {
        r.render(this);

        for (Component c : nestedComponents)
        {
            c.render(r);
        }

        renderedNestedComponents = true;

        r.render(this);
    }

    public String getTemplateName()
    {
        String templateName = getBaseTemplateName();
        if (renderedNestedComponents)
        {
            return templateName + "-end";
        }
        return templateName;
    }

    public abstract String getBaseTemplateName();
}
