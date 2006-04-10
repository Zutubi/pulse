package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 */
public class CronTriggerEditForm extends BaseForm
{
    public CronTriggerEditForm(WebTester tester)
    {
        super(tester);
    }

    public String getFormName()
    {
        return "trigger.cron.edit";
    }

    public String[] getFieldNames()
    {
        return new String[]{"trigger.name", "specification", "trigger.cron"};
    }
}
