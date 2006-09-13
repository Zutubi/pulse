package com.zutubi.pulse.form.mock;

import com.zutubi.pulse.form.descriptor.annotation.Password;
import com.zutubi.pulse.form.descriptor.annotation.Text;

/**
 * <class-comment/>
 */
public class MockAccount
{
    private String name;
    private String user;
    private String password;

    private String pass;

    @Text public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    // need to specify the type here since it will not be automatically picked up.
    @Password public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    @Text(size = 50) public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
