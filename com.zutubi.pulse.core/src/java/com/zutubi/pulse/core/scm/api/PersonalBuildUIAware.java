package com.zutubi.pulse.core.scm.api;

/**
 * Interface to implement for classes that are aware of the
 * {@link PersonalBuildUI}.  The UI may be used to give feedback to the user,
 * and to prompt them for input.
 *
 * @see PersonalBuildUIAwareSupport
 */
public interface PersonalBuildUIAware
{
    void setUI(PersonalBuildUI ui);    
}
