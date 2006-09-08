package com.zutubi.pulse.form.ui.components;

/**
 * <class-comment/>
 */
public class SubmitComponent extends Component
{
    public SubmitComponent(String name)
    {
        setName(name);
        setValue(name);
    }

    public String getTemplateName()
    {
        return "submit";
    }
}
