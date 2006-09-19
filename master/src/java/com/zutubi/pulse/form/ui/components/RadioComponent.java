package com.zutubi.pulse.form.ui.components;

import java.util.List;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class RadioComponent extends FieldComponent
{
    public String getTemplateName()
    {
        return "radiomap";
    }

    public void setList(Collection<String> keys)
    {
        addParameter("list", keys);
    }

}
