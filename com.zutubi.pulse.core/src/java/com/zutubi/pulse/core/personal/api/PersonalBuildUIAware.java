package com.zutubi.pulse.core.personal.api;

/**
 * Interface to implement for classes that are aware of the
 * {@link PersonalBuildUI}.  The UI may be used to give feedback to the user,
 * and to prompt them for input.
 *
 * @see com.zutubi.pulse.core.personal.PersonalBuildUIAwareSupport
 */
public interface PersonalBuildUIAware
{
    void setUI(PersonalBuildUI ui);    
}
