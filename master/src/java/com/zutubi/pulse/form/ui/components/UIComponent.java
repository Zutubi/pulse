package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.TemplateRendererContext;

/**
 * <class-comment/>
 */
public abstract class UIComponent extends Component
{
    public boolean end() throws Exception
    {
        // generate the template renderer context
        evaluateParameters();

        TemplateRendererContext templateContext = new TemplateRendererContext(getDefaultTemplate(), getParameters());
        context.getRenderer().render(templateContext);

        return false;
    }

    public abstract String getDefaultTemplate();

    protected void evaluateParameters()
    {
        Form form = (Form) findAncestor(Form.class);
        if (form != null)
        {
            String id = form.getId() + "_" + getParameters().get("name");
            setId(id);
        }

        evaluateExtraParameters();
    }

    protected void evaluateExtraParameters()
    {

    }

    public void setCssClass(String cssClass)
    {
        addParameter("cssClass", cssClass);
    }

    public void setCssStyle(String cssStyle)
    {
        addParameter("cssStyle", cssStyle);
    }

    public void setDisabled(String disabled)
    {
        addParameter("disabled", disabled);
    }

    public void setLabel(String label)
    {
        addParameter("label", label);
    }

    public void setLabelPosition(String labelPosition)
    {
        addParameter("labelposition", labelPosition);
    }

    public void setName(String name)
    {
        addParameter("name", name);
    }

    public void setRequired(boolean required)
    {
        addParameter("required", required);
    }

    public void setTabindex(int tabindex)
    {
        addParameter("tabindex", Integer.toString(tabindex));
    }

    public void setValue(String value)
    {
        addParameter("value", value);
    }

    public void setTitle(String title)
    {
        addParameter("title", title);
    }

    public void setOnclick(String onclick)
    {
        addParameter("onclick", onclick);
    }

    public void setOndblclick(String ondblclick)
    {
        addParameter("ondblclick", ondblclick);
    }

    public void setOnmousedown(String onmousedown)
    {
        addParameter("onmousedown", onmousedown);
    }

    public void setOnmouseup(String onmouseup)
    {
        addParameter("onmouseup", onmouseup);
    }

    public void setOnmouseover(String onmouseover)
    {
        addParameter("onmouseover", onmouseover);
    }

    public void setOnmousemove(String onmousemove)
    {
        addParameter("onmousemove", onmousemove);
    }

    public void setOnmouseout(String onmouseout)
    {
        addParameter("onmouseout", onmouseout);
    }

    public void setOnfocus(String onfocus)
    {
        addParameter("onfocus", onfocus);
    }

    public void setOnblur(String onblur)
    {
        addParameter("onblur", onblur);
    }

    public void setOnkeypress(String onkeypress)
    {
        addParameter("onkeypress", onkeypress);
    }

    public void setOnkeydown(String onkeydown)
    {
        addParameter("onkeydown", onkeydown);
    }

    public void setOnkeyup(String onkeyup)
    {
        addParameter("onkeyup", onkeyup);
    }

    public void setOnselect(String onselect)
    {
        addParameter("onselect", onselect);
    }

    public void setOnchange(String onchange)
    {
        addParameter("onchange", onchange);
    }
}
