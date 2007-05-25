package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ContactConfigurationFormatter implements Formatter<ContactConfiguration>
{
    public String[] columns()
    {
        return new String[]{"name", "uid"};
    }

    public String format(ContactConfiguration obj)
    {
        return obj.getName() + " " + obj.getUid();
    }
}
