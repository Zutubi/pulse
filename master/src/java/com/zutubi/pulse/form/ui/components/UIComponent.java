package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.RenderContext;
import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;

/**
 * <class-comment/>
 */
public abstract class UIComponent extends Component
{
    protected String cssClass;
    protected String cssStyle;
    protected String disabled;
    protected String label;
    protected String labelPosition;
    protected String name;
    protected Boolean required;
    protected Integer tabindex;
    protected String value;
    protected String title;

    // HTML scripting events attributes
    protected String onclick;
    protected String ondblclick;
    protected String onmousedown;
    protected String onmouseup;
    protected String onmouseover;
    protected String onmousemove;
    protected String onmouseout;
    protected String onfocus;
    protected String onblur;
    protected String onkeypress;
    protected String onkeydown;
    protected String onkeyup;
    protected String onselect;
    protected String onchange;


    public void render(RenderContext context, TemplateRenderer renderer) throws Exception
    {
        setContext(context);

        // generate the template renderer context
        evaluateParameters();

        TemplateRendererContext templateContext = new TemplateRendererContext(getDefaultTemplate(), getParameters(), context);
        renderer.render(templateContext);
    }

    public abstract String getDefaultTemplate();

    protected void evaluateParameters()
    {
        if (this.name != null)
        {
            addParameter("name", getText(name));
        }
        if (label != null)
        {
            addParameter("label", getText(label));
        }
        if (labelPosition != null)
        {
            addParameter("labelposition", labelPosition);
        }
        if (required != null)
        {
            addParameter("required", required);
        }
        if (disabled != null)
        {
            addParameter("disabled", disabled);
        }
        if (tabindex != null)
        {
            addParameter("tabindex", tabindex);
        }
        if (onclick != null)
        {
            addParameter("onclick", onclick);
        }
        if (ondblclick != null)
        {
            addParameter("ondblclick", ondblclick);
        }
        if (onmousedown != null)
        {
            addParameter("onmousedown", onmousedown);
        }
        if (onmouseup != null)
        {
            addParameter("onmouseup", onmouseup);
        }
        if (onmouseover != null)
        {
            addParameter("onmouseover", onmouseover);
        }
        if (onmousemove != null)
        {
            addParameter("onmousemove", onmousemove);
        }
        if (onmouseout != null)
        {
            addParameter("onmouseout", onmouseout);
        }
        if (onfocus != null)
        {
            addParameter("onfocus", onfocus);
        }
        if (onblur != null)
        {
            addParameter("onblur", onblur);
        }
        if (onkeypress != null)
        {
            addParameter("onkeypress", onkeypress);
        }
        if (onkeydown != null)
        {
            addParameter("onkeydown", onkeydown);
        }
        if (onkeyup != null)
        {
            addParameter("onkeyup", onkeyup);
        }
        if (onselect != null)
        {
            addParameter("onselect", onselect);
        }
        if (onchange != null)
        {
            addParameter("onchange", onchange);
        }
        if (cssClass != null)
        {
            addParameter("cssClass", cssClass);
        }
        if (cssStyle != null)
        {
            addParameter("cssStyle", cssStyle);
        }
        if (title != null)
        {
            addParameter("title", getText(title));
        }

        Form form = (Form) findAncestor(Form.class);
        if (form != null)
        {
            String id = form.getId() + "_" + getParameters().get("name");
            setId(id);
        }

        // if the value is set directly, then us it.
        if (!getParameters().containsKey("value"))
        {
            if (value != null)
            {
                addParameter("value", getText(value));
            }
            else if (name != null) // else, look up the value based on the name of this component.
            {
                addParameter("value", context.get(name));
            }
        }
        evaluateExtraParameters();
    }

    protected void evaluateExtraParameters()
    {

    }

    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }

    public void setCssStyle(String cssStyle)
    {
        this.cssStyle = cssStyle;
    }

    public void setDisabled(String disabled)
    {
        this.disabled = disabled;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setLabelPosition(String labelPosition)
    {
        this.labelPosition = labelPosition;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public void setTabindex(int tabindex)
    {
        this.tabindex = tabindex;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setOnclick(String onclick)
    {
        this.onclick = onclick;
    }

    public void setOndblclick(String ondblclick)
    {
        this.ondblclick = ondblclick;
    }

    public void setOnmousedown(String onmousedown)
    {
        this.onmousedown = onmousedown;
    }

    public void setOnmouseup(String onmouseup)
    {
        this.onmouseup = onmouseup;
    }

    public void setOnmouseover(String onmouseover)
    {
        this.onmouseover = onmouseover;
    }

    public void setOnmousemove(String onmousemove)
    {
        this.onmousemove = onmousemove;
    }

    public void setOnmouseout(String onmouseout)
    {
        this.onmouseout = onmouseout;
    }

    public void setOnfocus(String onfocus)
    {
        this.onfocus = onfocus;
    }

    public void setOnblur(String onblur)
    {
        this.onblur = onblur;
    }

    public void setOnkeypress(String onkeypress)
    {
        this.onkeypress = onkeypress;
    }

    public void setOnkeydown(String onkeydown)
    {
        this.onkeydown = onkeydown;
    }

    public void setOnkeyup(String onkeyup)
    {
        this.onkeyup = onkeyup;
    }

    public void setOnselect(String onselect)
    {
        this.onselect = onselect;
    }

    public void setOnchange(String onchange)
    {
        this.onchange = onchange;
    }
}
