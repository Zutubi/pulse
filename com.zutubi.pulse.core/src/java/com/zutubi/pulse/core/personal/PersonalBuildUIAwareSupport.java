package com.zutubi.pulse.core.personal;

import com.zutubi.pulse.core.personal.api.PersonalBuildUI;
import com.zutubi.pulse.core.personal.api.PersonalBuildUIAware;

/**
 * Helper base class for implementing {@link com.zutubi.pulse.core.personal.api.PersonalBuildUIAware}.
 */
public abstract class PersonalBuildUIAwareSupport implements PersonalBuildUIAware
{
    private PersonalBuildUI ui;

    public PersonalBuildUI getUI()
    {
        return ui;
    }

    public void setUI(PersonalBuildUI ui)
    {
        this.ui = ui;
    }
}
