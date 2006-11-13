package com.zutubi.validation.mock;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * <class-comment/>
 */
public class MockWallet implements Validateable
{
    private Object cc;
    private int money;

    public void validate(ValidationContext context)
    {
        // ensure that we have a credit card and some money, else we want another wallet.
        if (cc == null)
        {
            context.addFieldError("cc", "cc.required");
        }

        if (money < 100)
        {
            context.addFieldError("money", "money.min");
        }
    }
}
