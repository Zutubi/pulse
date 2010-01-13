package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.i18n.Messages;

/**
 * An I18N support class that understands the format of the properties classes
 * used by the upgrade tasks, providing convenience methods to access the I18N
 * strings.
 */
public class UpgradeTaskMessages
{
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";

    private final Messages I18N;

    public UpgradeTaskMessages(Class cls)
    {
        I18N = Messages.getInstance(cls);
    }

    public String getName()
    {
        return format(KEY_NAME);
    }

    public String getDescription()
    {
        return format(KEY_DESCRIPTION);
    }

    public String format(String name)
    {
        return I18N.format(name);
    }
}
