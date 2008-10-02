package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.validation.AbstractValidationTestCase;
import com.zutubi.tove.type.TypeException;

/**
 */
public class SelectedBuildsConditionConfigurationValidationTest extends AbstractValidationTestCase
{
    public void testConditionsSelected() throws TypeException
    {
        SelectedBuildsConditionConfiguration condition = new SelectedBuildsConditionConfiguration();
        condition.setIncludeChanges(true);
        validatedAndAssertValid("parent", "base", condition);
    }

    public void testNoConditionsSelected() throws TypeException
    {
        validateAndAssertInstanceErrors("parent", "base", new SelectedBuildsConditionConfiguration(), "please select at least one condition");
    }
}
