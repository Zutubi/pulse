package com.zutubi.pulse.form.ui;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.form.TextProvider;

import java.io.Writer;

/**
 * <class-comment/>
 */
public class RenderContext
{
    private TemplateRenderer renderer;

    private TextProvider textProvider;

    private OgnlValueStack stack;
    
    private Writer writer;

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

    public void push(Object obj)
    {
        getValueStack().push(obj);
    }

    public void pop()
    {
        getValueStack().pop();
    }

    private OgnlValueStack getValueStack()
    {
        if (stack == null)
        {
            stack = ActionContext.getContext().getValueStack();
        }
        return stack;
    }

    public Writer getWriter()
    {
        return writer;
    }

    public void setWriter(Writer writer)
    {
        this.writer = writer;
    }
}
