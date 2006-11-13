package com.zutubi.validation.mock;

import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Email;

/**
 * <class-comment/>
 */
public class MockAccount
{
    private String email;

    @Required @Email
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
