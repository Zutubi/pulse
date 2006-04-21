/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class SetPulseDataForm extends BaseForm
{
    public SetPulseDataForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "setup.data";
    }

    public String[] getFieldNames()
    {
        return new String[]{"data"};
    }
}
