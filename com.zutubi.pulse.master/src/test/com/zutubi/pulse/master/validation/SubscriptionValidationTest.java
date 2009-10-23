package com.zutubi.pulse.master.validation;

import com.zutubi.pulse.master.tove.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.RepeatedUnsuccessfulConditionConfiguration;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.tove.type.TypeException;
import com.zutubi.validation.ValidationException;

/**
 */
public class SubscriptionValidationTest extends AbstractValidationTestCase
{
    private NotifyConditionFactory notifyConditionFactory;

    protected void setUp() throws Exception
    {
        notifyConditionFactory = new NotifyConditionFactory();
        
        super.setUp();
    }

    public void testRepeatedUnsuccessfulConditionValid() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(10);
        validateAndAssertValid(condition);
    }

    public void testRepeatedUnsuccessfulConditionZeroAfter() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(0);

        validateAndAssertFieldErrors(condition, "after", "after.min");
    }

    public void testRepeatedUnsuccessfulConditionNegativeAfter() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(-1);

        validateAndAssertFieldErrors(condition, "after", "after.min");
    }

    public void testCustomConditionValid() throws TypeException, ValidationException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("changed or not success");

        validateAndAssertValid(condition);
    }

    public void testCustomConditionInvalid() throws TypeException, ValidationException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("invalid");

        validateAndAssertFieldErrors(condition, "customCondition", "line 1:1: unexpected token: invalid");
    }
}
