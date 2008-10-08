package com.zutubi.pulse.core.scm.api;

/**
 * Helper base class for implementing {@link PersonalBuildUIAware}.
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
