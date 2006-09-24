package com.zutubi.pulse.form.ui.components;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

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

        Map<String, String> values = new HashMap<String, String>();
        for (String str : strings)
        {
            values.put(str, str + ".label");
        }
        addParameter("listValues", values);
    }


}
