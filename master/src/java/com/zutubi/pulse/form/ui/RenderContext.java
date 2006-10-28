package com.zutubi.pulse.form.ui;

import com.zutubi.pulse.form.TextProvider;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.xwork.ActionContext;

/**
 * <class-comment/>
 */
public class RenderContext
{
    private TemplateRenderer renderer;

    private TextProvider textProvider;

    private OgnlValueStack stack;

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

    public Object get(String key)
    {
        return getValueStack().findValue(key);
    }

    private OgnlValueStack getValueStack()
    {
        if (stack == null)
        {
            stack = ActionContext.getContext().getValueStack();
        }
        return stack;
    }
}
