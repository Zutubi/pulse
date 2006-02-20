package com.cinnamonbob.web.user;

import com.cinnamonbob.model.YahooContactPoint;
import com.cinnamonbob.user.User;

/**
 *
 *
 */
public class CreateYahooContactPointAction extends AbstractCreateContactPointAction
{
    private YahooContactPoint contact = new YahooContactPoint();

    public YahooContactPoint getContact()
    {
        return contact;
    }

    public String execute()
    {
        User user = getUserManager().getUser(getUser());
        user.add(contact);

        return SUCCESS;
    }
}
