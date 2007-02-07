package com.zutubi.prototype.mock;

import com.zutubi.prototype.annotation.Password;
import com.zutubi.prototype.annotation.Text;
import com.zutubi.prototype.annotation.Form;

/**
 *
 *
 */
@Form(fieldOrder = {"email", "address", "name", "password"})
public class Sample
{
    private String name;
    private String address;
    private String email;
    private String password;

    @Password
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Text
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Text(size = 50)
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
