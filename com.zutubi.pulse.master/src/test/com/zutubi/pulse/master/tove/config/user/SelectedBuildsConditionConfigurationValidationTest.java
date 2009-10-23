package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.validation.AbstractValidationTestCase;
import com.zutubi.tove.type.TypeException;
import com.zutubi.validation.ValidationException;

/**
 */
public class SelectedBuildsConditionConfigurationValidationTest extends AbstractValidationTestCase
{
    public void testConditionsSelected() throws TypeException, ValidationException
    {
        SelectedBuildsConditionConfiguration condition = new SelectedBuildsConditionConfiguration();
        condition.setIncludeChanges(true);
        validateAndAssertValid(condition);
    }

    public void testNoConditionsSelected() throws TypeException, ValidationException
    {
        validateAndAssertInstanceErrors(new SelectedBuildsConditionConfiguration(), "please select at least one condition");
    }
}
