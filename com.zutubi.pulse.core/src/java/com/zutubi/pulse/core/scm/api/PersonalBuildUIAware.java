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
    /**
     * Setter used to inject the UI.  It will be injected before any other
     * methods are called on the aware instance.
     *
     * @param ui interface for communication with the user
     */
    void setUI(PersonalBuildUI ui);    
}
