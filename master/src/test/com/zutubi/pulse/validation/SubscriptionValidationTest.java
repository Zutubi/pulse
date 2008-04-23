package com.zutubi.pulse.validation;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.prototype.config.user.*;
import com.zutubi.pulse.prototype.config.user.contacts.EmailContactConfiguration;

/**
 */
public class SubscriptionValidationTest extends AbstractValidationTestCase
{
    private String subscriptionPath;
    private NotifyConditionFactory notifyConditionFactory;

    protected void setUp() throws Exception
    {
        notifyConditionFactory = new NotifyConditionFactory();

        super.setUp();
        UserConfiguration user = new UserConfiguration("admin", "admin");
        String userPath = configurationTemplateManager.insert(ConfigurationRegistry.USERS_SCOPE, user);

        EmailContactConfiguration contact = new EmailContactConfiguration();
        contact.setName("contact");
        contact.setAddress("foo@bar.com");
        String preferencesPath = PathUtils.getPath(userPath, "preferences");
        String contactPath = configurationTemplateManager.insert(PathUtils.getPath(preferencesPath, "contacts"), contact);
        contact = configurationProvider.get(contactPath, EmailContactConfiguration.class);

        ProjectSubscriptionConfiguration subscription = new ProjectSubscriptionConfiguration();
        subscription.setName("subscription");
        subscription.setContact(contact);
        subscription.setCondition(new AllBuildsConditionConfiguration());
        subscriptionPath = configurationTemplateManager.insert(PathUtils.getPath(preferencesPath, "subscriptions"), subscription);
    }

    public void testRepeatedUnsuccessfulConditionValid() throws TypeException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(10);
        validatedAndAssertValid(subscriptionPath, "condition", condition);
    }

    public void testRepeatedUnsuccessfulConditionZeroAfter() throws TypeException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(0);
        validateAndAssertFieldErrors(subscriptionPath, "condition", condition, "after", "after must be positive");
    }

    public void testRepeatedUnsuccessfulConditionNegativeAfter() throws TypeException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(-1);
        validateAndAssertFieldErrors(subscriptionPath, "condition", condition, "after", "after must be positive");
    }

    public void testCustomConditionValid() throws TypeException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("changed or not success");
        validatedAndAssertValid(subscriptionPath, "condition", condition);
    }

    public void testCustomConditionInvalid() throws TypeException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("invalid");
        validateAndAssertFieldErrors(subscriptionPath, "condition", condition, "customCondition", "line 1:1: unexpected token: invalid");
    }
}
