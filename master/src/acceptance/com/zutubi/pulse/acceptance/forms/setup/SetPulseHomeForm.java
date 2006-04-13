/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms.setup;

import com.zutubi.pulse.acceptance.forms.BaseForm;
import net.sourceforge.jwebunit.WebTester;

/**
 * <class-comment/>
 */
public class SetPulseHomeForm extends BaseForm
{
    public SetPulseHomeForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "setup.pulseHome";
    }

    public String[] getFieldNames()
    {
        return new String[]{"pulseHome"};
    }
}
