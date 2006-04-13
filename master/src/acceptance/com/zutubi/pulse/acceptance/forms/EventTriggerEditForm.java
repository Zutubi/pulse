/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class EventTriggerEditForm extends BaseForm
{
    public EventTriggerEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "trigger.event.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"trigger.name", "specification"};
    }
}
