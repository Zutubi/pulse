package com.zutubi.pulse.validation;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.prototype.config.user.*;
import com.zutubi.pulse.prototype.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.validation.validators.SubscriptionConditionValidator;
import com.zutubi.util.bean.DefaultObjectFactory;

/**
 */
public class SubscriptionValidationTest extends AbstractValidationTestCase
{
    private String subscriptionPath;

    protected void setUp() throws Exception
    {
        objectFactory = new DefaultObjectFactory()
        {
            @SuppressWarnings({ "unchecked" })
            public <V> V buildBean(Class<V> clazz) throws Exception
            {
                Object bean = super.buildBean(clazz);
                if(bean instanceof SubscriptionConditionValidator)
                {
                    ((SubscriptionConditionValidator)bean).setNotifyConditionFactory(new NotifyConditionFactory());
                }

                return (V) bean;
            }
        };

        super.setUp();
        UserConfiguration user = new UserConfiguration("admin", "admin");
        String userPath = configurationTemplateManager.insert(ConfigurationRegistry.USERS_SCOPE, user);

        EmailContactConfiguration contact = new EmailContactConfiguration();
        contact.setName("contact");
        contact.setAddress("foo@bar.com");
        String preferencesPath = PathUtils.getPath(userPath, "preferences");
        String contactPath = configurationTemplateManager.insert(PathUtils.getPath(preferencesPath, "contacts"), contact);
        contact = configurationTemplateManager.getInstance(contactPath, EmailContactConfiguration.class);

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
        assertValid(subscriptionPath, "condition", condition);
    }

    public void testRepeatedUnsuccessfulConditionZeroAfter() throws TypeException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(0);
        assertFieldErrors(subscriptionPath, "condition", condition, "after", "after must be positive");
    }

    public void testRepeatedUnsuccessfulConditionNegativeAfter() throws TypeException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(-1);
        assertFieldErrors(subscriptionPath, "condition", condition, "after", "after must be positive");
    }

    public void testCustomConditionValid() throws TypeException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setExpression("changed or not success");
        assertValid(subscriptionPath, "condition", condition);
    }

    public void testCustomConditionInvalid() throws TypeException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setExpression("invalid");
        assertFieldErrors(subscriptionPath, "condition", condition, "expression", "line 1:1: unexpected token: invalid");
    }
}
