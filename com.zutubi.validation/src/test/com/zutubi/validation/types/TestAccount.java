package com.zutubi.validation.types;

import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class TestAccount
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
