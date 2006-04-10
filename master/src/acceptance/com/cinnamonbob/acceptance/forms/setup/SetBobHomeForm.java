package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class SetBobHomeForm extends BaseForm
{
    public SetBobHomeForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "setup.bobHome";
    }

    public String[] getFieldNames()
    {
        return new String[]{"bobHome"};
    }
}
