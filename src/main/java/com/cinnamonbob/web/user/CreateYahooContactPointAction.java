package com.cinnamonbob.web.user;

import com.cinnamonbob.model.YahooContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class CreateYahooContactPointAction extends UserActionSupport
{
    private long user;

    private YahooContactPoint contact = new YahooContactPoint();

    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public YahooContactPoint getContact()
    {
        return contact;
    }

    public void validate()
    {

    }

    public String execute()
    {
        User user = getUserManager().getUser(this.user);
        user.add(contact);

        return SUCCESS;
    }
}
