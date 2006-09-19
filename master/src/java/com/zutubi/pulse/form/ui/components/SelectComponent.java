package com.zutubi.pulse.form.ui.components;

import java.util.List;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class SelectComponent extends FieldComponent
{
    public String getTemplateName()
    {
        return "select";
    }

    public void setList(Collection<String> strings)
    {
        addParameter("list", strings);
    }
}
