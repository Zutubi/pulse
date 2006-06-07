/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.tree;

import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class PopupFormAction extends ActionSupport
{
    private String formname;
    private String fieldname;

    public String getFormname()
    {
        return formname;
    }

    public void setFormname(String formname)
    {
        this.formname = formname;
    }

    public String getFieldname()
    {
        return fieldname;
    }

    public void setFieldname(String fieldname)
    {
        this.fieldname = fieldname;
    }
}
