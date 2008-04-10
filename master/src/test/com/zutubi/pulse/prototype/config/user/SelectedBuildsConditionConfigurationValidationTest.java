package com.zutubi.pulse.prototype.config.user;

import com.zutubi.pulse.validation.AbstractValidationTestCase;
import com.zutubi.prototype.type.TypeException;

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
